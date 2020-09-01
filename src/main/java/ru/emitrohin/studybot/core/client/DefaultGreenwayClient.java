package ru.emitrohin.studybot.core.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import okhttp3.JavaNetCookieJar;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.emitrohin.studybot.core.client.response.GreenwayAccountSearchResponse;

@Component
public class DefaultGreenwayClient implements GreenwayClient {

    private final OkHttpClient client;

    public DefaultGreenwayClient() {
        final CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        final JavaNetCookieJar cookieJar = new JavaNetCookieJar(cookieManager);
        this.client = new OkHttpClient.Builder().cookieJar(cookieJar).build();
    }

    @Override
    public Response getSearchAccountResponse(final String accountId) throws IOException {
        final var structureUrl = "https://2020.greenwaystart.com/my/business/structure/?search=%s&operation=checkRoot";

        final var request = new Request.Builder()
                .url(String.format(structureUrl, accountId))
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("user-agent",
                           "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
                .build();

        return client.newCall(request).execute();
    }

    @Override
    //TODO period is hardcored in the url
    public Response getGreenwayAccountResponse(final GreenwayAccountSearchResponse searchResult) throws IOException {
        final var structureUrl = "https://greenwaystart.com/my/business/structure/?PERIOD=44&FILTER=YES&LO=&SGO=&LEVEL=&activityFilter=disabled&RootUserVisual=%s&RootUser=%s&CITY=&CITY_ID=";
        final var rootUserVisual = searchResult.getNumber() + " - " + searchResult.getName();
        final var url = String.format(structureUrl,
                                      URLEncoder.encode(rootUserVisual, StandardCharsets.UTF_8.toString()),
                                      searchResult.getId());
        final var request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("user-agent",
                           "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
                .build();

        return client.newCall(request).execute();
    }

    @Override
    public Response getLoginResponse(final Map<String, String> data) throws IOException {
        final var requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        data.forEach(requestBody::addFormDataPart);

        final var request = new Request.Builder()
                .url("https://greenwaystart.com/s/l")
                .post(requestBody.build())
                .addHeader("user-agent",
                           "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
                .build();

        return client.newCall(request).execute();
    }

    @Override
    public Response getMyCabinet() throws IOException {
        final var request = new Request.Builder()
                .url("https://2020.greenwaystart.com/")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Connection","keep-alive")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("User-Agent",
                           "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
                .build();

        return client.newCall(request).execute();
    }

    @Override
    public Response logOut(final String id) throws IOException {
        final var url = "https://2020.greenwaystart.com/?LOGOUT=%s";
        final var formattedUrl = String.format(url, id);
        final var request = new Request.Builder()
                .url(formattedUrl)
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("user-agent",
                           "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
                .build();

        return client.newCall(request).execute();
    }
}
