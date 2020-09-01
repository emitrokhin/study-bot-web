package ru.emitrohin.studybot.core.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

public class LoginResponse {
    @JsonProperty("Message")
    private String message;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("ErrorCode")
    @Nullable
    private Integer errorCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}
