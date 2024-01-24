package com.ievlev.dataox.service;

import com.ievlev.dataox.dto.RequestDto;
import com.ievlev.dataox.dto.all_jobs_dto.JobFunctions;
import com.ievlev.dataox.dto.all_jobs_dto.JobHit;
import com.ievlev.dataox.dto.all_jobs_dto.SearchResultsWrapper;
import com.ievlev.dataox.model.Job;
import com.ievlev.dataox.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;


    // TODO: 24-Jan-24 обязательно добавить еще запрос на сортировку по локации и возможность указывать несколько локаций и несколько джоб функций в запросе
    // TODO: 24-Jan-24 возможно добавить сортировку по конкретной дате, но это уже будет выполняться в моей базе, а не на сайте
    public void getJobByJobFunction(RequestDto requestDto) {
        SearchResultsWrapper searchResultsWrapper = getResultsFromExternalApi(requestDto);
        if (searchResultsWrapper.getResults().isEmpty()) {
            // TODO: 23-Jan-24 вернуть NOT_FOUND и указать с каким запросом не найдено. или возвращать просто json и кодом ошибки что не найдено, а не писать это в таблицу

        }
        createJobs(searchResultsWrapper);
    }

    // TODO: 24-Jan-24 добавить проходку по всем страницам пагинации если нужно
    private SearchResultsWrapper getResultsFromExternalApi(RequestDto requestDto) {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://jobs.techstars.com/api/search/jobs";
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("networkId", "89")
                .queryParam("hitsPerPage", "20")
                .queryParam("page", "0")
//                .queryParam("filters", "(job_functions:\"Design\")")
                .queryParam("filters", "(job_functions:\"" + requestDto.getWorkFunction() + "\")")
                .queryParam("query", "")
                .build()
                .encode()
                .toUri();
        return restTemplate.getForObject(uri, SearchResultsWrapper.class);
    }

    // TODO: 24-Jan-24 возможно этот метод распаралелить (узнать насчет можно ли паралелить запись в базу, или собирать все в лист, и потом пачкой записывать в базу)
    private void createJobs(SearchResultsWrapper searchResultsWrapper) {
        List<JobHit> jobs = searchResultsWrapper.getResults().get(0).getHits();
        for (JobHit jobHit : jobs) { // TODO: 23-Jan-24 вот это можно и распаралелить, чтобы ходить на внешние ресурсы и заполнять джобы для сохранения в базу
            Job job = new Job();
            job.setPositionName(jobHit.getTitle());
            job.setOrganizationUrl(getUrlToOrganization(jobHit));// TODO: 23-Jan-24 not correct, тут должна быть ссылки из под кнопки apply
            job.setJobPageUrl("https://jobs.techstars.com/companies/playerdata/jobs/" + jobHit.getSlug()); // TODO: 23-Jan-24 проверить, должно возвращать вакансию именно на исходном сайте, даже и без описания, но не на внешнем
            job.setLogoLink(jobHit.getOrganization().getLogo_url());
            job.setOrganizationTitle(jobHit.getOrganization().getName());
            job.setLaborFunction(getLaborFunction(jobHit));
            job.setLocation(jobHit.getLocations().toString());
            job.setPostedDate(jobHit.getCreated_at()); //is is already in unix timestamp
            job.setDescription(getDescription(jobHit));
            job.setTagNames(getTagsFromJobHit(jobHit));
            job.setVacancyIdFromSite(jobHit.getObjectID());
            // TODO: 24-Jan-24 проверить чтобы добавлялись только уникальные джобы с уникальными айдишниками с сайта
            jobRepository.save(job);
        }


    }

    private String getTagsFromJobHit(JobHit jobHit) {
        int numberOfPeopleShort = jobHit.getOrganization().getHead_count();
        String stage = jobHit.getOrganization().getStage();
        List<String> industryTags = jobHit.getOrganization().getIndustry_tags();
        if(numberOfPeopleShort==0 & stage==null & industryTags.isEmpty()){
            return "NOT_FOUND";
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
            resultStringBuilder.append(", ").append(numberOfPeopleString);
        }
        if (stage != null) {
            resultStringBuilder.append(", ").append(stage);
        }
        return resultStringBuilder.toString();
    }

    private String getDescription(JobHit jobHit) {
        if (!jobHit.isHas_description()) {
            return "NOT_FOUND";
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
//        Element applyNowButton = document.selectFirst("#content > div.sc-beqWaB.eLTiFX > div.sc-dmqHEX.fPtgCq > div > div > div.sc-beqWaB.sc-gueYoa.MSxNX.MYFxR > div.sc-beqWaB.dqlQzp > a");
        Element applyNowButton = document.selectFirst("a[data-testid=button]");
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
