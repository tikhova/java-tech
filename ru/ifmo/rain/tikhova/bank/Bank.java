package ru.ifmo.rain.tikhova.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    Person createPerson(String name, String surname, String passport) throws RemoteException;

    Person getPerson(String passport, boolean local) throws RemoteException;
}
