package ru.emitrohin.studybot.core.client;

import java.io.IOException;
import java.util.Map;

import okhttp3.Response;
import ru.emitrohin.studybot.core.client.response.GreenwayAccountSearchResponse;

public interface GreenwayClient {
    Response getSearchAccountResponse(final String accountId) throws IOException;

    Response getGreenwayAccountResponse(final GreenwayAccountSearchResponse searchResult) throws IOException;

    Response getLoginResponse(final Map<String, String> formData) throws IOException;

    Response getMyCabinet() throws IOException;

    Response logOut(final String id) throws IOException;
}
