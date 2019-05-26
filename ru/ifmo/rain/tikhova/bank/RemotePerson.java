package ru.ifmo.rain.tikhova.bank;

import java.rmi.server.UnicastRemoteObject;

public class RemotePerson extends PersonImpl {
    RemotePerson(String name, String surname, String passport, int port) {
        super(name, surname, passport, port);
    }

    @Override
    public Account addAccount(String accountId) throws Exception {
        AccountImpl account = (AccountImpl) super.addAccount(accountId);
        UnicastRemoteObject.exportObject(account, port);
        return account;
    }

}
