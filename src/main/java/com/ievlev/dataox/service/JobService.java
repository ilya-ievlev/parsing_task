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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final GoogleApiUtil googleApiUtil;
    private GoogleSheetModel googleSheetModel;
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final int MILISEC_IN_DAY = 86_400_000;


    @PostConstruct
    // TODO: 25-Jan-24 узнать у ментора. метод должен выполняться один раз при первом запросе и больше не выполняться
    public void createGoogleSheet() {
        googleSheetModel = googleApiUtil.createGoogleSheet();
    }


    // TODO: 24-Jan-24 в последнюю очередь сделать лимитер запросов чтобы не ддосить внешний сервер запросами
    // TODO: 24-Jan-24 обязательно добавить еще запрос на сортировку по локации и возможность указывать несколько локаций и несколько джоб функций в запросе
    // TODO: 24-Jan-24 возможно добавить сортировку по конкретной дате, но это уже будет выполняться в моей базе, а не на сайте
    public void getJobByJobFunction(RequestDto requestDto) {
        SearchResultsWrapper firstSearchResultsWrapper = getResultsFromExternalApi(requestDto, 0);
        if (firstSearchResultsWrapper.getResults().get(0).getHits().isEmpty()) {
            // TODO: 23-Jan-24 вернуть NOT_FOUND и указать с каким запросом не найдено. или возвращать просто json и кодом ошибки что не найдено, а не писать это в таблицу
        }
        createJobFromHit(firstSearchResultsWrapper);
        int numberOfPages = firstSearchResultsWrapper.getResults().get(0).getNbPages();
        if (numberOfPages > 1) {
            for (int i = 1; i < numberOfPages; i++) {
                // TODO: 24-Jan-24 проверять все ли данные сохранились, и что делать с данными которые по какой то причине не сохранились в базу и соответственно в таблицу
                createJobFromHit(getResultsFromExternalApi(requestDto, i));
            }
        }
    }

    // TODO: 25-Jan-24 поменять айдишник генерируемый на айдишник вакансии с сайта
    // TODO: 24-Jan-24 сделать 2й вариант отправки запросов через okhttp (они так хотят)
    private SearchResultsWrapper getResultsFromExternalApi(RequestDto requestDto, int numberOfPage) {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://jobs.techstars.com/api/search/jobs";
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("networkId", "89")
                .queryParam("hitsPerPage", "1000") //1000 is a max limit provided by external api
                .queryParam("page", String.valueOf(numberOfPage))
                .queryParam("filters", getFilters(requestDto))
                .queryParam("query", "")
                .build()
                .encode()
                .toUri();
        return restTemplate.getForObject(uri, SearchResultsWrapper.class);
    }


    private ConcurrentLinkedQueue<JobHit> checkJobHitByIdInDB(List<JobHit> jobList) {
        ConcurrentLinkedQueue<JobHit> jobConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        for (JobHit job : jobList) {
            if (jobRepository.findById(Long.parseLong(job.getObjectID())).isEmpty()) {
                jobConcurrentLinkedQueue.add(job); // TODO: 25-Jan-24 потом в эти джобы в многопоточке доставляем все остальное, и потом в однопоточке пишем в базу и таблицу
            }
        }
        return jobConcurrentLinkedQueue;
    }

    // TODO: 25-Jan-24 а если передадут например 2 одинаковых значения, и в другие фильтры тоже
    private List<JobHit> sortJobHitByDatePosted(RequestDto requestDto, List<JobHit> jobList) {
        List<String> requiredDates = requestDto.getDatesToShow();
        if (requiredDates.isEmpty()) {
            return jobList;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        List<TimeLimit> parsedDatesUnix = new ArrayList<>();
        for (String str : requiredDates) {
            TimeLimit timeLimit = new TimeLimit();// TODO: 25-Jan-24 смотреть какая вакансия попадает между этих двух переменных
            long parsedDate = dateFormat.parse(str).getTime() / 1000;
            timeLimit.setStart(parsedDate);
            timeLimit.setEnd(parsedDate + MILISEC_IN_DAY);
            parsedDatesUnix.add(timeLimit); // TODO: 25-Jan-24 завернуть в необработываемый ексепшен и отловить в ексепшен хендлере
        }

        List<JobHit> sortedByTimeJobHits = new ArrayList<>();

        for (JobHit job : jobList) { // TODO: 25-Jan-24 эти операции тоже можно делать в многопоточности вроде бы (не точно)
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
                result.append("searchable_locations:\"" + requestDto.getLocations().get(i) + "\"");
                if (requestDto.getLocations().size() == 1 || i == requestDto.getWorkFunctions().size() - 1) { // TODO: 24-Jan-24 возможно можно оставить только 2 условие, если оно выполняется то 1 будет правдой всегда
                    break;
                }
                result.append(" OR ");
            }
            result.append(")");
        }
        return result.toString();
    }

    // TODO: 24-Jan-24 возможно этот метод распаралелить (узнать насчет можно ли паралелить запись в базу, или собирать все в лист, и потом пачкой записывать в базу)
    private void createJobFromHit(SearchResultsWrapper searchResultsWrapper) { // TODO: 25-Jan-24 мы не будем на прямую из врапера делать джобы. хиты сначала надо обработать 
        List<JobHit> jobs = searchResultsWrapper.getResults().get(0).getHits();
        for (JobHit jobHit : jobs) { // TODO: 23-Jan-24 вот это можно и распаралелить, чтобы ходить на внешние ресурсы и заполнять джобы для сохранения в базу
            Job job = new Job();
            job.setPositionName(jobHit.getTitle());
            job.setOrganizationUrl(getUrlToOrganization(jobHit));// TODO: 23-Jan-24 not correct, тут должна быть ссылки из под кнопки apply
            job.setJobPageUrl("https://jobs.techstars.com/companies/playerdata/jobs/" + jobHit.getSlug()); // TODO: 23-Jan-24 проверить, должно возвращать вакансию именно на исходном сайте, даже и без описания, но не на внешнем
            job.setLogoLink(getLogoUrl(jobHit));
            job.setOrganizationTitle(jobHit.getOrganization().getName());
            job.setLaborFunction(getLaborFunction(jobHit));
            job.setLocation(jobHit.getLocations().toString());
            job.setPostedDate(jobHit.getCreated_at()); //is is already in unix timestamp
            job.setDescription(getDescription(jobHit));
            job.setTagNames(getTagsFromJobHit(jobHit));
            job.setVacancyIdFromSite(Long.parseLong(jobHit.getObjectID())); // TODO: 25-Jan-24 а что будет если тут парсед ексепшен вылетит
            // TODO: 24-Jan-24 проверить чтобы добавлялись только уникальные джобы с уникальными айдишниками с сайта
//            jobRepository.save(job); // TODO: 24-Jan-24 это не тут делать, нужно сначала делать проверку не существует ли уже такой джобы, потом пытаться сохранить в базу и таблизу. проверить сохранилось ли в таблице и базе, если одно не сохранилось то удалить второе и сказать об этом пользователю исключением или написать в json ответе
        }
    }

    private void saveJobToDatabaseAndSheets(Job job, int numberOfRawToSave) { // TODO: 25-Jan-24 а если одно что-то не вставится, то как откатить или остановить процесс?
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

    private String getTagsFromJobHit(JobHit jobHit) { // TODO: 24-Jan-24 убрать запятую если нет первых тегов, только последние
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
            ioException.printStackTrace(); // TODO: 23-Jan-24 сделать что-то с этим исключением, и еще с парой в других частях кода
        } // TODO: 24-Jan-24 преверить чтобы документ не был нул
//        Elements descriptionElements = document.select("#content > div.sc-beqWaB.eLTiFX > div.sc-dmqHEX.fPtgCq > div > div > div.sc-beqWaB.fmCCHr > div");
        Elements descriptionElements = document.select("div[data-testid=careerPage]");
        if (descriptionElements == null) {
            descriptionElements = document.select("#content > div.sc-beqWaB.eFnOti > div.sc-dmqHEX.fPtgCq > div > div > div.sc-beqWaB.fmCCHr"); // TODO: 24-Jan-24 проверить единственная ли это ситуация когда невозможно подтянуть описание( стянуть максимально вакансий, отсортировать по описанию и посмотреть чтобы не было пустых) и написать комент почему и зачем именно так
        }
        return descriptionElements.text();// TODO: 23-Jan-24 по заданию нужно сохранить оригинальную разметку. как это сделать?
    } // TODO: 24-Jan-24 возможно не .text() a .html(). он должен хранить и текст, и разметку

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

    private String getLaborFunction(JobHit jobHit) { // TODO: 24-Jan-24 так же прописать что если не найдено тут и на всех полях, даже на названии вакансии
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
