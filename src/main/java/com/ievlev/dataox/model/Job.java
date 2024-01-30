package com.ievlev.dataox.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Table(name = "jobs")
public class Job {
    @Id
    @Min(1)
    @Column(name = "vacancy_id_from_site")
    @NotNull
    private Long vacancyIdFromSite;

    @Column(name = "position_name")
    private String positionName;

    @Column(name = "job_page_url")
    private String jobPageUrl;

    @Column(name = "organization_url")
    private String organizationUrl;

    @Column(name = "logo_link")
    private String logoLink;

    @Column(name = "organization_title")
    private String organizationTitle;

    @Column(name = "labor_function")
    private String laborFunction;

    @Column(name = "location")
    private String location;

    @Column(name = "posted_date")
    private long postedDate;

    @Column(name = "description")
    private String description;

    @Column(name = "tags_names")
    private String tagNames;


    public Job() {
    }

    public Job(Long vacancyIdFromSite, String positionName, String jobPageUrl, String organizationUrl, String logoLink, String organizationTitle, String laborFunction, String location, long postedDate, String description, String tagNames) {
        this.vacancyIdFromSite = vacancyIdFromSite;
        this.positionName = positionName;
        this.jobPageUrl = jobPageUrl;
        this.organizationUrl = organizationUrl;
        this.logoLink = logoLink;
        this.organizationTitle = organizationTitle;
        this.laborFunction = laborFunction;
        this.location = location;
        this.postedDate = postedDate;
        this.description = description;
        this.tagNames = tagNames;
    }
}
