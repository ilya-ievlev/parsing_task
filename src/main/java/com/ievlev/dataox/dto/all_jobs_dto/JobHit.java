package com.ievlev.dataox.dto.all_jobs_dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Jacksonized
@Builder
public class JobHit {
    private long created_at;
    private List<String> locations;
    private Organization organization;
    private String source;
    private String slug;
    private String title;
    private String url;
    private boolean featured;
    private boolean has_description;
    private String objectID;

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isHas_description() {
        return has_description;
    }

    public void setHas_description(boolean has_description) {
        this.has_description = has_description;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}
