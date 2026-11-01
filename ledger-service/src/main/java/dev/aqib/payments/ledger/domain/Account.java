package dev.aqib.payments.ledger.domain;

/**
 * An account holds a balance and delegates every decision to its product's policies.
 * Withdraw = amount + fee; the limit policy sees the balance *after*; a rejected withdrawal changes nothing.
 */
public final class Account {

    // TODO: fields

    public Account(String id, Product product, String currency) {
        throw new UnsupportedOperationException("TODO");
    }

    public void deposit(Money amount) { throw new UnsupportedOperationException("TODO"); }

    public void withdraw(Money amount) { throw new UnsupportedOperationException("TODO"); }

    public void applyMonthlyInterest() { throw new UnsupportedOperationException("TODO"); }

    public String id() { throw new UnsupportedOperationException("TODO"); }

    public Product product() { throw new UnsupportedOperationException("TODO"); }

    public Money balance() { throw new UnsupportedOperationException("TODO"); }
}
