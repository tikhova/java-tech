package ru.ifmo.rain.tikhova.bank;

import java.io.Serializable;

public class AccountImpl implements Account, Serializable {
    final private String passport;
    final private String subId;
    final private String id;
    private int amount = 0;

    AccountImpl(String passport, String subId) {
        this.passport = passport;
        this.subId = subId;
        this.id = passport + ":" + subId;
    }

    AccountImpl(String passport, String subId, int amount) {
        this(passport, subId);
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int n) {
        amount = n;
    }
}
