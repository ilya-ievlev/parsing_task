package com.ievlev.dataox.mapper;

import com.ievlev.dataox.aggregators.HtmlDataAggregator;
import com.ievlev.dataox.dto.all_jobs_dto.JobFunctions;
import com.ievlev.dataox.dto.all_jobs_dto.JobHit;
import com.ievlev.dataox.dto.all_jobs_dto.Organization;
import com.ievlev.dataox.model.Job;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class JobHitToJobMapper {
    private final HtmlDataAggregator htmlDataAggregator;
    private static final String NOT_FOUND = "NOT_FOUND";

    public Job createJobFromHit(JobHit jobHit) {
        Job job = new Job();
        job.setPositionName(jobHit.getTitle());
        job.setOrganizationUrl(htmlDataAggregator.getUrlToOrganization(jobHit.getSlug()));
        job.setJobPageUrl("https://jobs.techstars.com/companies/playerdata/jobs/" + jobHit.getSlug());
        job.setLogoLink(getLogoUrl(jobHit.getOrganization().getLogo_url()));
        job.setOrganizationTitle(jobHit.getOrganization().getName());
        job.setLaborFunction(getLaborFunction(jobHit.get_highlightResult().getJob_functions()));
        job.setLocation(jobHit.getLocations().toString());
        job.setPostedDate(jobHit.getCreated_at()); //it is already in unix timestamp
        job.setDescription(htmlDataAggregator.getDescription(jobHit.getSlug(), jobHit.isHas_description()));
        job.setTagNames(getTagsFromJobHit(jobHit.getOrganization()));
        job.setVacancyIdFromSite(Long.parseLong(jobHit.getObjectID()));
        return job;
    }

    private static String getLogoUrl(String logoUrl) {
        if (logoUrl == null) {
            return NOT_FOUND;
        }
        return logoUrl;
    }

    private String getTagsFromJobHit(Organization organization) {
        int numberOfPeopleShort = organization.getHead_count();
        String stage = organization.getStage();
        List<String> industryTags = organization.getIndustry_tags();
        if (numberOfPeopleShort == 0 & stage == null & industryTags.isEmpty()) {
            return NOT_FOUND;
        }
        String numberOfPeopleString;
        switch (numberOfPeopleShort) {
            case 1:
                numberOfPeopleString = "1 - 10";
                break;
            case 2:
                numberOfPeopleString = "11 - 50";
                break;
            case 3:
                numberOfPeopleString = "51 - 200";
                break;
            case 4:
                numberOfPeopleString = "201 - 1000";
                break;
            case 5:
                numberOfPeopleString = "1001 - 5000";
                break;
            case 6:
                numberOfPeopleString = "5001+";
                break;
            default:
                numberOfPeopleString = null;
        }
        StringBuilder resultStringBuilder = new StringBuilder();
        for (int i = 0; i < industryTags.size(); i++) {
            resultStringBuilder.append(industryTags.get(i));
            if (industryTags.size() == 1 || i == industryTags.size() - 1) {
                break;
            }
            resultStringBuilder.append(", ");
        }
        if (numberOfPeopleString != null) {
            if (!industryTags.isEmpty()) {
                resultStringBuilder.append(", ");
            }
            resultStringBuilder.append(numberOfPeopleString);
        }
        if (stage != null) {
            if (!industryTags.isEmpty() || numberOfPeopleString != null) {
                resultStringBuilder.append(", ");
            }
            resultStringBuilder.append(stage);
        }
        return resultStringBuilder.toString();
    }

    private String getLaborFunction(List<JobFunctions> jobFunctionsList) {
        StringBuilder resultStringBuilder = new StringBuilder();
        for (int i = 0; i < jobFunctionsList.size(); i++) {
            resultStringBuilder.append(jobFunctionsList.get(i).getValue());
            if (jobFunctionsList.size() == 1 ||
                    i == jobFunctionsList.size() - 1) {// this is created to avoid putting a comma after the last element
                break;
            }
            resultStringBuilder.append(", ");
        }
        return resultStringBuilder.toString();
    }
}
