package ru.ifmo.rain.tikhova.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BankRemote implements Remote, Bank {
    private Map<String, RemotePerson> clients = new ConcurrentHashMap<>();
    private final int port;

    BankRemote(final int port) {
        this.port = port;
    }

    public Person createPerson(String name, String surname, String passport) throws RemoteException {
        if (clients.containsKey(passport)) {
            System.out.println("Client with passport " + passport + " already exists");
            return clients.get(passport);
        }
        RemotePerson person = new RemotePerson(name, surname, passport, port);
        clients.put(passport, person);
        UnicastRemoteObject.exportObject(person, port);
        return person;
    }

    public Person getPerson(String passport, boolean local) {
        if (local) return new LocalPerson(clients.get(passport));
        else return clients.get(passport);
    }
}