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
public class Organization {
    private int id;
    private String name;
    private String logo_url;
    private String slug;
    private List<String> topics;
    private List<String> industry_tags;
    private int head_count;
    private String stage;

}
