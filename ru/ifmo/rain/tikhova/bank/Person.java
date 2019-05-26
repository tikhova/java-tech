package ru.ifmo.rain.tikhova.bank;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote, Serializable {
    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    String getPassport() throws RemoteException;

    Account addAccount(String accountId) throws Exception;

    Account getAccount(String accountId) throws RemoteException;
}
