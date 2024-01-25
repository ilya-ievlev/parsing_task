package com.ievlev.dataox.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Jacksonized
@Builder
@Data
public class RequestDto {
    private List<String> workFunctions;
    private List<String> locations;
    private List<String> datesToShow;
}
