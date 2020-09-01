package ru.emitrohin.studybot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ru.emitrohin.studybot.core.client.GreenwayAccount;
import ru.emitrohin.studybot.core.client.GreenwayAccountHandler;

@Component
public class AuthorizationHandler {

    private static final int ACTIVATION_AMOUNT = 50;

    final private GreenwayAccountHandler       handler;
    final private Map<String, GreenwayAccount> existingAccounts;

    final private Set<String>                  bannedAccounts = Set.of("10126178");

    @Autowired
    public AuthorizationHandler(final GreenwayAccountHandler handler) {
        this.handler = handler;
        this.existingAccounts = new HashMap<>();
    }

    public boolean authorize(final String login, final String date) {
        if (bannedAccounts.contains(login)) {
            return false;
        }

        if (existingAccounts.containsKey(login)) {
            return authorizeDate(login, date);
        }

        final var user = handler.getGreenwayAccount(login);
        if (user.isPresent()) {
            existingAccounts.put(login, user.get());
            return authorizeDate(login, date);
        }

        return false;
    }

    private boolean authorizeDate(final String login, final String date) {
        if (existingAccounts.containsKey(login)) {
            return existingAccounts.get(login).getDate().equals(date);
        }
        return false;
    }

    public boolean isUserActivated(final String login) {
        final var user = handler.getGreenwayAccount(login);
        return user.filter(greenwayAccount -> greenwayAccount.getAmount() >= ACTIVATION_AMOUNT)
                   .isPresent();
    }
}
