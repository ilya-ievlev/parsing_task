package com.ievlev.dataox.dto.all_jobs_dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
@Builder
@Data
public class SearchResult {
    private List<JobHit> hits;
    private int nbHits;
    private int page;
    private int nbPages;
    private int hitsPerPage;
}
