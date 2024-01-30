package com.ievlev.dataox.api_client;

import com.ievlev.dataox.dto.RequestDto;
import com.ievlev.dataox.dto.all_jobs_dto.SearchResultsWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@AllArgsConstructor
public class TechstarsApiClient {
    private final RestTemplate restTemplate;

    public SearchResultsWrapper getJobsFromExternalApi(RequestDto requestDto, int numberOfPage) {
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
}
