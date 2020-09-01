package ru.emitrohin.studybot.core.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GreenwayAccountSearchResponse {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("NUMBER")
    private String number;

    @JsonProperty("NAME")
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
