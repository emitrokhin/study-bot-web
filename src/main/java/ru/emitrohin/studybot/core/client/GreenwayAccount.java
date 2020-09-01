package ru.emitrohin.studybot.core.client;

import java.util.StringJoiner;

public class GreenwayAccount {

    private final String accountId;
    private final String date;
    private final Integer amount;

    public GreenwayAccount(final String accountId, final String date, final Integer amount) {
        this.accountId = accountId;
        this.date = date;
        this.amount = amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getDate() {
        return date;
    }

    public Integer getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GreenwayAccount.class.getSimpleName() + "[", "]")
                .add("accountId='" + accountId + "'")
                .add("date='" + date + "'")
                .add("amount=" + amount)
                .toString();
    }
}
