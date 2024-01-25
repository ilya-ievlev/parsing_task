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
    private List<String> locations; // TODO: 24-Jan-24 что если какое то поле не предоставлено, или пустое, или содержит нулы
    private List<String> datesToShow;
}
