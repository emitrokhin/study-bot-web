package ru.emitrohin.studybot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import okhttp3.OkHttpClient;
import ru.emitrohin.studybot.core.client.GreenwayClient;

@Service
@Path("/greenway")
public class GreenwayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreenwayService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final static String AUTHORIZE_WEBHOOK = "https://app.botmother.com/api/bot/action/mjMfTtzvk/DVBGCqCfBXDVBYCzCYBxCcDPDRDxPMCoZCpUCLD8o6BTD6nCeBhDJCYGDZDoWDYB";
    private final static String NOT_AUTHORIZED_WEBHOOK = "https://app.botmother.com/api/bot/action/zm4rVIvV-/BJBcBckBfBACeCMBUBJBXB5C07G9DtDYCCTpBRBlDlDrrDZBmLD-B6BnCfCaGCFC";
    private final static String ACTIVATE_WEBHOOK = "https://app.botmother.com/api/bot/action/9ZV1GRsGd/DZkC0yB9CwBKCnBVQrDiDgDeB5BxQBABlBzYCvDzC0DJBSCECBB9BJBLBNTBKBBY";
    private final static String NOT_ACTIVATE_WEBHOOK = "https://app.botmother.com/api/bot/action/ybyquKCbC/LBSDqBnDNC_DzDHCPCNBGzBQDE0CeeD9D5hBW1BCrBsDnD_BuCkBTB8DsBABq4CZ";

    private final static String PAYLOAD = "{\n" +
            "  \"platform\": \"any\",\n" +
            "  \"users\": [ \"%s\" ],\n" +
            "  \"data\": {\n" +
            "  }\n" +
            "}";

    private final static String GREENWAY_ID = "greenway_id";
    private final static String REGISTER_DATE = "register_date";
    private final static String USER_ID = "user_id";

    private AuthorizationHandler handler;

    @Autowired
    public GreenwayService(AuthorizationHandler handler) {
        this.handler = handler;
    }

    @POST
    @Path("/authorize")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authorize(String jsonRequest) {
        try {
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<>() {};
            HashMap<String, String> payload = MAPPER.readValue(jsonRequest, typeRef);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder request = HttpRequest.newBuilder()
                                                     .header("Content-Type", "application/json")
                                                     .POST(HttpRequest.BodyPublishers
                                                                   .ofString(String.format(PAYLOAD, payload.get(USER_ID))));
            if (handler.authorize(payload.get(GREENWAY_ID), payload.get(REGISTER_DATE))) {
                client.send(request.uri(URI.create(AUTHORIZE_WEBHOOK)).build(),
                            HttpResponse.BodyHandlers.ofString());
            } else {
                client.send(request.uri(URI.create(NOT_AUTHORIZED_WEBHOOK)).build(),
                            HttpResponse.BodyHandlers.ofString());
            }
            return Response.status(200).build();
        }
        catch (Exception e) {
            LOGGER.error("{}, {}", e.getMessage(), e.fillInStackTrace());
            return Response.status(500).build();
        }
    }

    @POST
    @Path("/activate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response activate(String jsonRequest) {
        try {
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<>() {};
            HashMap<String, String> payload = MAPPER.readValue(jsonRequest, typeRef);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder request = HttpRequest.newBuilder()
                                                     .header("Content-Type", "application/json")
                                                     .POST(HttpRequest.BodyPublishers
                                                                   .ofString(String.format(PAYLOAD, payload.get(USER_ID))));
            if (handler.isUserActivated(payload.get(GREENWAY_ID))) {
                client.send(request.uri(URI.create(ACTIVATE_WEBHOOK)).build(),
                            HttpResponse.BodyHandlers.ofString());
            } else {
                client.send(request.uri(URI.create(NOT_ACTIVATE_WEBHOOK)).build(),
                            HttpResponse.BodyHandlers.ofString());
            }
            return Response.status(200).build();
        }
        catch (Exception e) {
            LOGGER.error("{}, {}", e.getMessage(), e.fillInStackTrace());
            return Response.status(500).build();
        }    }
}
