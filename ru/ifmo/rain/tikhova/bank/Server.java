package ru.ifmo.rain.tikhova.bank;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private final static int PORT = 8888;

    public static void main(String[] args) {
        // Регистрация банка в RMI registry
        try {
            Bank bank = new BankRemote(PORT);
            Bank stub = (Bank) UnicastRemoteObject.exportObject(bank, 0);

            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.rebind("bank", stub);
        } catch (RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
        }
        System.out.println("Server started");
    }
}
