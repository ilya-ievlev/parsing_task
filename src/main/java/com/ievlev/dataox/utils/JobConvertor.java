package com.ievlev.dataox.utils;

import com.ievlev.dataox.model.Job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class JobConvertor {
    private static String convertUnixDateToReadableDate(long unixDate) {
        Date date = new Date(unixDate * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }

    public static List<List<Object>> convertJobToListOfListOfObject(Job job) {
        List<List<Object>> dataToBeInserted = new ArrayList<>();
        dataToBeInserted.add(Collections.singletonList(job.getJobPageUrl()));
        dataToBeInserted.add(Collections.singletonList(job.getPositionName()));
        dataToBeInserted.add(Collections.singletonList(job.getOrganizationUrl()));
        dataToBeInserted.add(Collections.singletonList(job.getLogoLink()));
        dataToBeInserted.add(Collections.singletonList(job.getOrganizationTitle()));
        dataToBeInserted.add(Collections.singletonList(job.getLaborFunction()));
        dataToBeInserted.add(Collections.singletonList(job.getLocation()));
        dataToBeInserted.add(Collections.singletonList(convertUnixDateToReadableDate(job.getPostedDate())));
        dataToBeInserted.add(Collections.singletonList(job.getDescription()));
        dataToBeInserted.add(Collections.singletonList(job.getTagNames()));
        return dataToBeInserted;
    }
}
