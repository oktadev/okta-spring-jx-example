package com.okta.developer.holdingsapi;

public class Holding {
    private String crypto;
    private String currency;
    private String amount;

    public String getCrypto() {
        return crypto;
    }

    public Holding setCrypto(String crypto) {
        this.crypto = crypto;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public Holding setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public String getAmount() {
        return amount;
    }

    public Holding setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public String toString() {
        return "Holding{" +
                "crypto='" + crypto + '\'' +
                ", currency='" + currency + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}