package com.ievlev.dataox.controller;

import com.ievlev.dataox.dto.RequestDto;
import com.ievlev.dataox.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @GetMapping("api/v1/jobs")
    public String getJobs(@RequestBody RequestDto requestDto) {
        return jobService.processUserRequest(requestDto);
    }
}
