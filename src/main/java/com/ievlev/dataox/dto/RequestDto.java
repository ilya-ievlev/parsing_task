package com.ievlev.dataox.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Data
public class RequestDto {
    private String workFunction;
    // TODO: 22-Jan-24 узнать что значит work functions, job categories, в чем разница
    private String jobLocation; // TODO: 22-Jan-24 хорошо ли делать нулом если фильтрация по локации не нужна?
    private Long postingDate; // TODO: 22-Jan-24 тот же принцип с нулом, но вопрос в каком формате принимать от пользователя дату? и если он передаст в неправильном то кидать исключение
    // TODO: 23-Jan-24 возможно принимать только день, без уточнения времени до секунд

    private boolean sortInDescendingOrderByDate;
}
