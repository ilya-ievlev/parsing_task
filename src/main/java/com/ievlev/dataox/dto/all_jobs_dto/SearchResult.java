package com.ievlev.dataox.dto.all_jobs_dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
@Builder
public class SearchResult {
    private List<JobHit> hits;
    private int nbHits;
    private int page;
    private int nbPages;
    private int hitsPerPage;

    public List<JobHit> getHits() {
        return hits;
    }

    public void setHits(List<JobHit> hits) {
        this.hits = hits;
    }

    public int getNbHits() {
        return nbHits;
    }

    public void setNbHits(int nbHits) {
        this.nbHits = nbHits;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getNbPages() {
        return nbPages;
    }

    public void setNbPages(int nbPages) {
        this.nbPages = nbPages;
    }

    public int getHitsPerPage() {
        return hitsPerPage;
    }

    public void setHitsPerPage(int hitsPerPage) {
        this.hitsPerPage = hitsPerPage;
    }
}
