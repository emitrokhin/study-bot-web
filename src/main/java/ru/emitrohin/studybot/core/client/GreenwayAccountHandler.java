package ru.emitrohin.studybot.core.client;

import com.google.common.hash.Hashing;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import okhttp3.Response;
import ru.emitrohin.studybot.core.client.response.GreenwayAccountResponse;
import ru.emitrohin.studybot.core.client.response.GreenwayAccountSearchResponse;
import ru.emitrohin.studybot.core.client.response.LoginResponse;
import ru.emitrohin.studybot.core.client.response.SessionResponse;

@Component
public class GreenwayAccountHandler {

    // todo  <title>Технические работы :: GreenWay</title>

    private static final Logger       LOG    = LoggerFactory.getLogger(GreenwayAccountHandler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String SESSION_KEY_EVENT  = "greenway.sessionKey";
    private static final String FIND_ACCOUNT_EVENT = "greenway.searchAccount";
    private static final String GET_ACCOUNT_EVENT  = "greenway.getAccount";

    private static final int      MAX_RETRIES = 3;
    private static final Duration DELAY       = Duration.ofMillis(200);

    private final GreenwayClient client;
    private final String         rootLoginID;
    private final String         password;

    private String sessionKey = null;

    @Autowired
    public GreenwayAccountHandler(final GreenwayClient client) {
        this.client = client;
        this.rootLoginID = System.getenv("GWLOGIN");
        this.password = System.getenv("GWPASSWORD");
        authorize();
    }

    public Optional<GreenwayAccount> getGreenwayAccount(final String loginId) {
        try {
            final var result = findUserByAccountId(loginId);
            if (result.isPresent()) {

                final var response = Failsafe.with(retryPolicy(loginId, GET_ACCOUNT_EVENT))
                                             .get(() -> client.getGreenwayAccountResponse(result.get()));

                final var html = Jsoup.parse(Objects.requireNonNull(response.body()).string());
                final var elements = html.select("table tr td");

                final var id = elements.get(3).text();
                final var date = elements.get(4).text().substring(0, 8); //trim last chars, example 18.02.198
                final var amount = Integer.parseInt(elements.get(6)
                                                            .text()
                                                            .substring(0, elements.get(6).text().length() - 2)
                                                            .replace(" ", "")); //trim 2 last char, example 40,0

                return Optional.of(new GreenwayAccount(id, date, amount));
            }
        }
        catch (Exception e) {
            final var message = String.format("LoginID %s: %s failed", loginId, GET_ACCOUNT_EVENT);
            LOG.error(message, e);
        }
        return Optional.empty();
    }

    public Optional<GreenwayAccountSearchResponse> findUserByAccountId(final String loginId) {
        final GreenwayAccountResponse result;
        String responseMessage = "";
        try {
            final var response = Failsafe.with(retryPolicy(loginId, FIND_ACCOUNT_EVENT))
                                         .get(() -> client.getSearchAccountResponse(loginId));
            responseMessage = Objects.requireNonNull(response.body()).string();
            result = MAPPER.readValue(responseMessage, GreenwayAccountResponse.class);
        }
        // failed probably due to cookies expiration
        catch (JsonParseException e) {
            final var message = String.format("LoginID %s: %s failed, Message: %s",
                                              loginId,
                                              FIND_ACCOUNT_EVENT,
                                              responseMessage);
            LOG.warn(message, e);
            authorize();
            return findUserByAccountId(loginId);
        }
        catch (Exception e) {
            final var message = String.format("LoginID %s: %s failed", loginId, FIND_ACCOUNT_EVENT);
            LOG.error(message, e);
            return Optional.empty();
        }

        return result.getCount() == 0 ? Optional.empty() : Optional.of(result.getResults().get(0));
    }

    private RetryPolicy<Object> retryPolicy(final String loginId, final String event) {
        return new RetryPolicy<>().withDelay(DELAY)
                                  .withMaxRetries(MAX_RETRIES)
                                  .onRetry(e -> LOG.info("LoginID {}: {} failed. Retrying ...", loginId, event))
                                  .onFailure(e -> LOG.error(String.format("LoginID %s: %s failure.", loginId, event),
                                                            e.getFailure()));
    }

    private void authorize() {
        LOG.info("Login {}: starting authorization", rootLoginID);

        if (this.sessionKey == null) {
            final var sessionKey = getSessionKey();
            sessionKey.ifPresent(key -> {
                this.sessionKey = sessionKey.get();
                final var loginRequestData = Map.of("type", "auth",
                                                    "action", "login",
                                                    "NAME", rootLoginID,
                                                    "PASSWORD", hashPassword(key));

                final var loginResponse = Failsafe.with(retryPolicy(rootLoginID, SESSION_KEY_EVENT))
                                                  .get(() -> client.getLoginResponse(loginRequestData));

                if (getErrorCode(loginResponse).isPresent()) {
                    final var message = String.format("LoginID %s: %s failed", rootLoginID, SESSION_KEY_EVENT);
                    LOG.error(message);
                    throw new RuntimeException("Authorization failed");
                }
                LOG.info("Login {}: {} successful authorization", rootLoginID, SESSION_KEY_EVENT);
            });
        } else {
            LOG.info("Login {}: {} session key exists. successful authorization", rootLoginID, SESSION_KEY_EVENT);
        }
    }

    private Optional<String> getSessionKey() {
        final var keyRequestData = Map.of("type", "auth",
                                          "action", "sessionkey");

        final var response = Failsafe.with(retryPolicy(rootLoginID, SESSION_KEY_EVENT))
                                     .get(() -> client.getLoginResponse(keyRequestData));

        final SessionResponse sessionKeyMessage;
        try {
            if (response.body() == null) {
                LOG.warn("Login {}: {} response body is null, please check server response model", rootLoginID, SESSION_KEY_EVENT);
                return Optional.empty();
            }

            var responseString = response.body().string();
            if (responseString.contains("OPEN") && this.sessionKey != null) {
                return Optional.of(this.sessionKey);
            }

            sessionKeyMessage = MAPPER.readValue(responseString, SessionResponse.class);
        }
        catch (IOException e) {
            final var message = String.format("LoginID %s: %s failed", rootLoginID, SESSION_KEY_EVENT);
            LOG.error(message, e);
            return Optional.empty();
        }

        return Optional.of(sessionKeyMessage.getKey());
    }

    private String hashPassword(final String sessionKey) {
        final var pass = Hashing.md5().hashString(password, StandardCharsets.UTF_8).toString();
        return Hashing.md5().hashString(sessionKey + pass, StandardCharsets.UTF_8).toString();
    }

    private Optional<Integer> getErrorCode(final Response response) {
        final LoginResponse loginResponse;
        try {
            //FIXME duplicated in getSessionKey()
            if (response.body() == null) {
                LOG.warn("Login {}: {} response body is null, please check server response model", rootLoginID, SESSION_KEY_EVENT);
                return Optional.empty();
            }
            loginResponse = MAPPER.readValue(response.body().string(), LoginResponse.class);
        }
        catch (IOException e) {
            final var message = String.format("LoginID %s: %s failed", rootLoginID, SESSION_KEY_EVENT);
            LOG.error(message, e);
            return Optional.empty();
        }

        return Optional.ofNullable(loginResponse.getErrorCode());
    }
}
