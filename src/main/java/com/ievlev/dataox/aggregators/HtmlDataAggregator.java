package com.ievlev.dataox.aggregators;

import com.ievlev.dataox.exception.IncorrectConnectionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HtmlDataAggregator {
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String URL_BASE = "https://jobs.techstars.com/companies/playerdata/jobs/";
    private static final String DESCRIPTION_CSS_QUERY = "div[data-testid=careerPage]";
    private static final String BUTTON_CSS_QUERY = "a[data-testid=button]";
    private static final String HREF_ATTRIBUTE = "href";

    public String getDescription(String slug, boolean hasDescription) {
        if (!hasDescription) {
            return NOT_FOUND;
        }
        String url = URL_BASE + slug;
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException ioException) {
            throw new IncorrectConnectionException(ioException);
        }
        Elements descriptionElements = document.select(DESCRIPTION_CSS_QUERY);
        if (descriptionElements == null) {
            return NOT_FOUND;
        }
        return descriptionElements.text();
    }

    public String getUrlToOrganization(String slug) {
        String url = URL_BASE + slug;

        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException ioException) {
            throw new IncorrectConnectionException(ioException);
        }
        Element applyNowButton = document.selectFirst(BUTTON_CSS_QUERY);
        if (applyNowButton == null) {
            return NOT_FOUND;
        }
        return applyNowButton.attr(HREF_ATTRIBUTE);
    }
}
