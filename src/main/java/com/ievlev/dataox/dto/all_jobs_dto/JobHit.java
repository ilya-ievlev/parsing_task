package com.ievlev.dataox.dto.all_jobs_dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class JobHit {
    private long created_at;
    private List<String> locations;
    private Organization organization;
    private String source;
    private String slug;
    private String title;
    private String url;
    private boolean featured;
    private boolean has_description;
    private String objectID;

    private HighlightResult _highlightResult;
}
