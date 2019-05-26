package ru.ifmo.rain.tikhova.bank;

import java.io.Serializable;

class LocalPerson extends PersonImpl implements Serializable {

    LocalPerson(PersonImpl other) {
        super(other.name, other.surname, other.passport, other.port);
        other.accounts.keySet().
                forEach(x -> this.accounts.put(x, new AccountImpl(this.passport, x, other.accounts.get(x).getAmount())));
    }
}
