package com.ievlev.dataox.dto.all_jobs_dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Jacksonized
@Builder
public class Organization {
    private int id;
    private String name;
    private String logo_url;
    private String slug;
    private List<String> topics;
    private List<String> industry_tags;
    private int head_count;
    private String stage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo_url() {
        return logo_url;
    }

    public void setLogo_url(String logo_url) {
        this.logo_url = logo_url;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getIndustry_tags() {
        return industry_tags;
    }

    public void setIndustry_tags(List<String> industry_tags) {
        this.industry_tags = industry_tags;
    }

    public int getHead_count() {
        return head_count;
    }

    public void setHead_count(int head_count) {
        this.head_count = head_count;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }
}
