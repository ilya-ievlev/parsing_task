package com.ievlev.dataox.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;

@Entity
@Data
@Table(name = "jobs")
public class Job {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Min(1)
    private Long id;

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

    @Column(name = "vacancy_id_from_site")
    private String vacancyIdFromSite;

    public Job() {
    }

    public Job(Long id, String positionName, String jobPageUrl, String organizationUrl, String logoLink,
               String organizationTitle, String laborFunction, String location, long postedDate, String description, String tagNames, String vacancyIdFromSite) {
        this.id = id;
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
        this.vacancyIdFromSite = vacancyIdFromSite;
    }

    public Job(String positionName, String jobPageUrl, String organizationUrl, String logoLink, String organizationTitle, String laborFunction, String location,
               long postedDate, String description, String tagNames, String vacancyIdFromSite) {
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
        this.vacancyIdFromSite = vacancyIdFromSite;
    }
}
