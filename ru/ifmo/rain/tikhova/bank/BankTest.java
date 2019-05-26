package ru.ifmo.rain.tikhova.bank;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class BankTest {
    private final static int PORT = 8888;
    private static Bank bank;
    private static Registry registry;

    private String name = "Mariya";
    private String surname = "Tikhova";
    private String passport = "0000000000";

    @BeforeClass
    public static void beforeClass() throws RemoteException {
        registry = LocateRegistry.createRegistry(PORT);
    }

    @Before
    public void before() throws RemoteException, NotBoundException {
        bank = new BankRemote(PORT);
        Bank stub = (Bank) UnicastRemoteObject.exportObject(bank, 0);
        registry.rebind("bank", stub);
    }

    @Test
    public void createAndGetInformation() throws RemoteException {
        Person person = bank.createPerson(name, surname, passport);
        assertEquals(name, person.getName());
        assertEquals(surname, person.getSurname());
        assertEquals(passport, person.getPassport());
    }

    @Test
    public void getPerson() throws RemoteException {
        addPerson();
        Person remotePerson = bank.getPerson(passport, false);
        Person localPerson = bank.getPerson(passport, true);
        assertTrue(localPerson instanceof LocalPerson);
        assertTrue(remotePerson instanceof RemotePerson);

        assertEquals(localPerson.getName(), remotePerson.getName());
        assertEquals(localPerson.getSurname(), remotePerson.getSurname());
        assertEquals(passport, localPerson.getPassport());
        assertEquals(passport, remotePerson.getPassport());
    }

    @Test
    public void createMultipleAccounts() throws RemoteException {
        addPerson();
        addAccounts();
        Person person = bank.getPerson(passport, false);
        IntStream.range(1, 10).forEach(x -> {
            try {
                assertNotNull(person.getAccount(String.valueOf(x)));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void modifyMultipleAccounts() throws RemoteException {
        addPerson();
        addAccounts();
        Person person = bank.getPerson(passport, false);
        IntStream.range(1, 10).forEach(x -> {
            try {
                person.getAccount(String.valueOf(x)).setAmount(x);
                assertEquals(x, person.getAccount(String.valueOf(x)).getAmount());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void checkAccountIds() throws RemoteException {
        addPerson();
        addAccounts();
        Person person = bank.getPerson(passport, false);
        IntStream.range(1, 10).forEach(x -> {
            try {
                assertEquals(person.getPassport().concat(":").concat(String.valueOf(x)),
                        person.getAccount(String.valueOf(x)).getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void checkChanges() throws RemoteException {
        addPerson();
        addAccounts();
        Person remote1 = bank.getPerson(passport, false);
        Person remote2 = bank.getPerson(passport, false);
        Person local1 = bank.getPerson(passport, true);
        Person local2 = bank.getPerson(passport, true);

        int newAmount = 100;
        remote1.getAccount("1").setAmount(newAmount);
        // Changes received
        assertEquals(newAmount, remote1.getAccount("1").getAmount());
        // Remote people received changes
        assertEquals(remote1.getAccount("1").getAmount(), remote2.getAccount("1").getAmount());
        // Only remote people received changes
        assertNotEquals(remote1.getAccount("1").getAmount(), local1.getAccount("1").getAmount());
        Person local3 = bank.getPerson(passport, true);
        // Local person created after changes knows about them
        assertEquals(remote1.getAccount("1").getAmount(), local3.getAccount("1").getAmount());

        local1.getAccount("2").setAmount(newAmount);
        // Changes received
        assertEquals(newAmount, local1.getAccount("2").getAmount());
        // Only this local person received them
        assertNotEquals(local1.getAccount("2").getAmount(), remote1.getAccount("2").getAmount());
        assertNotEquals(local1.getAccount("2").getAmount(), local2.getAccount("2").getAmount());
    }

    void addPerson() throws RemoteException {
        bank.createPerson(name, surname, passport);
    }

    void addAccounts() throws RemoteException {
        Person person = bank.getPerson(passport, false);
        IntStream.range(1, 10).forEach(x -> {
            try {
                person.addAccount(String.valueOf(x));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

