package com.ievlev.dataox.dto.all_jobs_dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HighlightResult {
    private List<JobFunctions> job_functions;
}
