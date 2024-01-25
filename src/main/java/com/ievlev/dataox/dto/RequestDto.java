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
    // TODO: 22-Jan-24 узнать что значит work functions, job categories, в чем разница
    // TODO: 23-Jan-24 возможно принимать только день, без уточнения времени до секунд
    private List<String> locations; // TODO: 24-Jan-24 что если какое то поле не предоставлено, или пустое, или содержит нулы
    private List<String> datesToShow; // TODO: 24-Jan-24 парсить, и если он передаст в неправильном то кидать исключение
}
