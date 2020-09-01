package ru.emitrohin.studybot.core.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GreenwayAccountResponse {

    @JsonProperty("count")
    private int count;

    @JsonProperty("results")
    private List<GreenwayAccountSearchResponse> results;

    @JsonProperty("html")
    private String html;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<GreenwayAccountSearchResponse> getResults() {
        return results;
    }

    public void setResults(List<GreenwayAccountSearchResponse> results) {
        this.results = results;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
