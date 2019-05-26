package ru.ifmo.rain.tikhova.bank;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class PersonImpl implements Person {
    protected String name;
    String surname;
    String passport;
    Map<String, AccountImpl> accounts;
    protected final int port;

    PersonImpl(String name, String surname, String passport, int port) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.port = port;
        this.accounts = new ConcurrentHashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPassport() {
        return passport;
    }

    public Account addAccount(String accountId) throws Exception {
        if (accounts.containsKey(accountId)) {
            System.out.println("AccountImpl " + accountId + " already exists");
            return accounts.get(accountId);
        }
        AccountImpl account = new AccountImpl(this.passport, accountId);
        accounts.put(accountId, account);
        return account;
    }

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }
}
