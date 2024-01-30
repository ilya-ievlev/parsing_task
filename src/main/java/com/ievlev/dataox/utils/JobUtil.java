package com.ievlev.dataox.utils;

import com.ievlev.dataox.dto.RequestDto;
import com.ievlev.dataox.dto.all_jobs_dto.JobHit;
import com.ievlev.dataox.model.TimeLimit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class JobUtil {
    private static final int SEC_IN_DAY = 86_400;
    private static final String DATE_PATTERN = "dd-MM-yyyy";

    public static void validateRequestDto(RequestDto requestDto) {
        if (requestDto.getDatesToShow() == null || requestDto.getLocations() == null || requestDto.getWorkFunctions() == null) {
            throw new IllegalArgumentException("request dto must contain all necessary objects");
        }
    }

    public static List<JobHit> sortJobHitByDatePosted(RequestDto requestDto, List<JobHit> jobList) {
        List<String> requiredDates = requestDto.getDatesToShow();
        if (requiredDates.isEmpty()) {
            return jobList;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        List<TimeLimit> parsedDatesUnix = new ArrayList<>();
        for (String str : requiredDates) {
            TimeLimit timeLimit = new TimeLimit();
            long parsedDate = 0;
            try {
                parsedDate = dateFormat.parse(str).getTime() / 1000;
            } catch (ParseException e) {
                throw new IllegalArgumentException("there is something wrong with the date you entered" + e);
            }
            timeLimit.setStart(parsedDate);
            timeLimit.setEnd(parsedDate + SEC_IN_DAY);
            parsedDatesUnix.add(timeLimit);
        }

        List<JobHit> sortedByTimeJobHits = new ArrayList<>();

        for (JobHit job : jobList) {
            long jobCreatedTime = job.getCreated_at();
            for (TimeLimit timeLimit : parsedDatesUnix) {
                if (jobCreatedTime > timeLimit.getStart() && jobCreatedTime < timeLimit.getEnd()) {
                    sortedByTimeJobHits.add(job);
                }
            }
        }
        return sortedByTimeJobHits;
    }
}
