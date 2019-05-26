package ru.ifmo.rain.tikhova.bank;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) throws Exception {
        // Параметры
        if (args.length < 5) {
            System.out.println("Invalid number of arguments");
            System.out.println("Usage: [name] [surname] [passportId] [accountId] [amount] [[serial]]");
            return;
        }
        String name = args[0];
        String surname = args[1];
        String passportId = args[2];
        String accountId = args[3];
        int amount = Integer.valueOf(args[4]);
        boolean isLocal = args.length > 5 && args[5].equals("1");

        // Получение ссылки на банк
        Registry registry = LocateRegistry.getRegistry(8888);
        Bank bank = (Bank) registry.lookup("bank");

        // Создание клиента
        Person person = bank.getPerson(passportId, isLocal);
        if (person == null) {
            System.out.println("Creating client");
            person = bank.createPerson(name, surname, passportId);
        } else {
            System.out.println("Checking client's information");
            // Операции с клиентом
            if (!name.equals(person.getName()) || !surname.equals(person.getSurname())) {
                System.out.println("Client's information is incorrect");
                return;
            }
        }

        // Операции с аккаунтом
        Account account = person.getAccount(accountId);
        if (account == null) {
            System.out.println("Creating account");
            account = person.addAccount(accountId);
        } else {
            System.out.println("AccountImpl already exists");
        }

        System.out.println("Initial balance: " + account.getAmount());
        account.setAmount(amount);
        System.out.println("New balance: " + account.getAmount());
    }
}
