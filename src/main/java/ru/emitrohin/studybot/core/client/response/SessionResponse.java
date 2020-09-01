package ru.emitrohin.studybot.core.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionResponse {
    @JsonProperty("Key")
    private String key;

    public String getKey() {
        return key;
    }
}
