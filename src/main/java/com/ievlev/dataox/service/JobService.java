package com.ievlev.dataox.service;

import com.ievlev.dataox.dto.RequestDto;
import com.ievlev.dataox.dto.all_jobs_dto.JobFunctions;
import com.ievlev.dataox.dto.all_jobs_dto.JobHit;
import com.ievlev.dataox.dto.all_jobs_dto.SearchResultsWrapper;
import com.ievlev.dataox.model.GoogleSheetModel;
import com.ievlev.dataox.model.Job;
import com.ievlev.dataox.model.TimeLimit;
import com.ievlev.dataox.repository.JobRepository;
import com.ievlev.dataox.utils.GoogleApiUtil;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JobService {
    private static final String DATE_PATTERN = "dd-MM-yyyy";
    private final JobRepository jobRepository;
    private final GoogleApiUtil googleApiUtil;
    private GoogleSheetModel googleSheetModel;
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final int SEC_IN_DAY = 86_400;

    private int lastRowInSheets;


    @PostConstruct
    public void createGoogleSheet() {
        googleSheetModel = googleApiUtil.createGoogleSheet();
    }


    public String processUserRequest(RequestDto requestDto) {
        SearchResultsWrapper firstSearchResultsWrapper = getResultsFromExternalApi(requestDto, 0);
        ConcurrentLinkedQueue<Job> jobListToWrite = process(firstSearchResultsWrapper, requestDto);
//        int numberOfPages = firstSearchResultsWrapper.getResults().get(0).getNbPages();
//        if (numberOfPages > 1) {
//            for (int i = 1; i < numberOfPages; i++) {
//                SearchResultsWrapper searchResultsWrapper = getResultsFromExternalApi(requestDto, i);
//                jobListToWrite.addAll(process(searchResultsWrapper, requestDto));
//            }
//        }
        for (Job job : jobListToWrite) {
            lastRowInSheets++;
            saveJobToDatabaseAndSheets(job, lastRowInSheets);
        }
        return googleSheetModel.getUrlToGoogleSheet();
    }

    private ConcurrentLinkedQueue<Job> process(SearchResultsWrapper searchResultsWrapper, RequestDto requestDto) {
        List<JobHit> hitsSortedByTime = sortJobHitByDatePosted(requestDto, searchResultsWrapper.getResults().get(0).getHits());
        List<JobHit> hitsCheckedForRepetition = checkJobHitByIdInDB(hitsSortedByTime);
//        List<Job> processedJobsReadyToBeWritten = new ArrayList<>();
//        for (JobHit jobHit : hitsCheckedForRepetition) {
//            processedJobsReadyToBeWritten.add(createJobFromHit(jobHit));
//        }
        ConcurrentLinkedQueue<Job> processedJobsReadyToBeWritten = new ConcurrentLinkedQueue<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (JobHit jobHit : hitsCheckedForRepetition) {
            executor.execute(() -> processedJobsReadyToBeWritten.add(createJobFromHit(jobHit)));
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                System.err.println("Some tasks did not finish before the timeout.");
                // Handle the case where tasks did not finish
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return processedJobsReadyToBeWritten;
    }

    private SearchResultsWrapper getResultsFromExternalApi(RequestDto requestDto, int numberOfPage) {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://jobs.techstars.com/api/search/jobs";
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("networkId", "89")
                .queryParam("hitsPerPage", "100") //1000 is a max limit provided by external api
                .queryParam("page", String.valueOf(numberOfPage))
                .queryParam("filters", getFilters(requestDto))
                .queryParam("query", "")
                .build()
                .encode()
                .toUri();
        return restTemplate.getForObject(uri, SearchResultsWrapper.class);
    }


    private List<JobHit> checkJobHitByIdInDB(List<JobHit> jobList) {
        List<JobHit> checkedJobHits = new ArrayList<>();
        for (JobHit job : jobList) {
            if (jobRepository.findById(Long.parseLong(job.getObjectID())).isEmpty()) {
                checkedJobHits.add(job);
            }
        }
        return checkedJobHits;
    }

    private List<JobHit> sortJobHitByDatePosted(RequestDto requestDto, List<JobHit> jobList) {
        List<String> requiredDates = requestDto.getDatesToShow();
        if (requiredDates.isEmpty()) {
            return jobList;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        List<TimeLimit> parsedDatesUnix = new ArrayList<>();
        for (String str : requiredDates) {
            TimeLimit timeLimit = new TimeLimit();
            long parsedDate = 0;
            try {
                parsedDate = dateFormat.parse(str).getTime() / 1000;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            timeLimit.setStart(parsedDate);
            timeLimit.setEnd(parsedDate + SEC_IN_DAY);
            parsedDatesUnix.add(timeLimit);
        }

        List<JobHit> sortedByTimeJobHits = new ArrayList<>();

        for (JobHit job : jobList) {
            long jobCreatedTime = job.getCreated_at();
            for (TimeLimit timeLimit : parsedDatesUnix) {
                if (jobCreatedTime > timeLimit.getStart() && jobCreatedTime < timeLimit.getEnd()) {
                    sortedByTimeJobHits.add(job);
                }
            }
        }
        return sortedByTimeJobHits;
    }


    private String getFilters(RequestDto requestDto) {
        if (requestDto.getWorkFunctions().isEmpty()) {
            throw new IllegalArgumentException("work functions can't be empty");
        }
        StringBuilder result = new StringBuilder();
        result.append("(");
        for (int i = 0; i < requestDto.getWorkFunctions().size(); i++) {
            result.append("job_functions:\"").append(requestDto.getWorkFunctions().get(i)).append("\"");
            if (requestDto.getWorkFunctions().size() == 1 || i == requestDto.getWorkFunctions().size() - 1) {
                break;
            }
            result.append(" OR ");
        }
        result.append(")");
        if (!requestDto.getLocations().isEmpty()) {
            result.append(" AND (");
            for (int i = 0; i < requestDto.getLocations().size(); i++) {
                result.append("searchable_locations:\"").append(requestDto.getLocations().get(i)).append("\"");
                if (requestDto.getLocations().size() == 1 || i == requestDto.getWorkFunctions().size() - 1) {
                    break;
                }
                result.append(" OR ");
            }
            result.append(")");
        }
        return result.toString();
    }

    private Job createJobFromHit(JobHit jobHit) {
        Job job = new Job();
        job.setPositionName(jobHit.getTitle());
        job.setOrganizationUrl(getUrlToOrganization(jobHit));
        job.setJobPageUrl("https://jobs.techstars.com/companies/playerdata/jobs/" + jobHit.getSlug());
        job.setLogoLink(getLogoUrl(jobHit));
        job.setOrganizationTitle(jobHit.getOrganization().getName());
        job.setLaborFunction(getLaborFunction(jobHit));
        job.setLocation(jobHit.getLocations().toString());
        job.setPostedDate(jobHit.getCreated_at()); //it is already in unix timestamp
        job.setDescription(getDescription(jobHit));
        job.setTagNames(getTagsFromJobHit(jobHit));
        job.setVacancyIdFromSite(Long.parseLong(jobHit.getObjectID()));
        return job;
    }

    private void saveJobToDatabaseAndSheets(Job job, int numberOfRawToSave) {
        jobRepository.save(job);
        googleApiUtil.addJobToSheet(job, numberOfRawToSave, googleSheetModel);
    }

    private String getLogoUrl(JobHit jobHit) {
        String logoUrl = jobHit.getOrganization().getLogo_url();
        if (logoUrl == null) {
            return NOT_FOUND;
        }
        return logoUrl;
    }

    private String getTagsFromJobHit(JobHit jobHit) {
        int numberOfPeopleShort = jobHit.getOrganization().getHead_count();
        String stage = jobHit.getOrganization().getStage();
        List<String> industryTags = jobHit.getOrganization().getIndustry_tags();
        if (numberOfPeopleShort == 0 & stage == null & industryTags.isEmpty()) {
            return NOT_FOUND;
        }
        String numberOfPeopleString;
        switch (numberOfPeopleShort) {
            case 1:
                numberOfPeopleString = "1 - 10";
                break;
            case 2:
                numberOfPeopleString = "11 - 50";
                break;
            case 3:
                numberOfPeopleString = "51 - 200";
                break;
            case 4:
                numberOfPeopleString = "201 - 1000";
                break;
            case 5:
                numberOfPeopleString = "1001 - 5000";
                break;
            case 6:
                numberOfPeopleString = "5001+";
                break;
            default:
                numberOfPeopleString = null;
        }
        StringBuilder resultStringBuilder = new StringBuilder();
        for (int i = 0; i < industryTags.size(); i++) {
            resultStringBuilder.append(industryTags.get(i));
            if (industryTags.size() == 1 || i == industryTags.size() - 1) {
                break;
            }
            resultStringBuilder.append(", ");
        }
        if (numberOfPeopleString != null) {
            if (!industryTags.isEmpty()) {
                resultStringBuilder.append(", ");
            }
            resultStringBuilder.append(numberOfPeopleString);
        }
        if (stage != null) {
            if (!industryTags.isEmpty() || numberOfPeopleString != null) {
                resultStringBuilder.append(", ");
            }
            resultStringBuilder.append(stage);
        }
        return resultStringBuilder.toString();
    }

    private String getDescription(JobHit jobHit) {
        if (!jobHit.isHas_description()) {
            return NOT_FOUND;
        }
        String url = "https://jobs.techstars.com/companies/playerdata/jobs/" + jobHit.getSlug();
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Elements descriptionElements = document.select("div[data-testid=careerPage]");
        if (descriptionElements == null) {
            return NOT_FOUND;
        }
        return descriptionElements.text();
    }

    private String getUrlToOrganization(JobHit jobHit) {
        String url = "https://jobs.techstars.com/companies/playerdata/jobs/" + jobHit.getSlug();
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Element applyNowButton = document.selectFirst("a[data-testid=button]");
        if (applyNowButton == null) {
            return NOT_FOUND;
        }
        return applyNowButton.attr("href");
    }

    private String getLaborFunction(JobHit jobHit) {
        StringBuilder resultStringBuilder = new StringBuilder();
        List<JobFunctions> jobFunctionsList = jobHit.get_highlightResult().getJob_functions();
//        for(JobFunctions jobFunctions: jobFunctionsList){
        for (int i = 0; i < jobFunctionsList.size(); i++) {
            resultStringBuilder.append(jobFunctionsList.get(i).getValue());
            if (jobFunctionsList.size() == 1) {
                break;
            }
            if (i == jobFunctionsList.size() - 1) { // this is created to avoid putting a comma after the last element
                break;
            }
            resultStringBuilder.append(", ");
        }
        return resultStringBuilder.toString();
    }

}
