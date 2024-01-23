package com.ievlev.dataox.service;

import com.ievlev.dataox.dto.RequestDto;
import com.ievlev.dataox.dto.all_jobs_dto.SearchResult;
import com.ievlev.dataox.model.Job;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class JobService {
    public void getJobByJobFunction(RequestDto requestDto) {
        String apiUrl = "https://jobs.techstars.com/api/search/jobs?networkId=89&hitsPerPage=20&page=0&filters=%28job_functions%3A\"Design\"%29&query=";
        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);
        SearchResult searchResult = restTemplate.getForObject(apiUrl, SearchResult.class);
//        if(responseEntity.getStatusCode().is2xxSuccessful()){
//            String jsonResponse = responseEntity.getBody();
//            System.out.println(jsonResponse);
//        }
    }
}
