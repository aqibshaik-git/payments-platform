package dev.aqib.payments.ledger.domain;

import java.util.Objects;

/**
 * An account holds a balance and delegates every decision to its product's policies.
 * Withdraw = amount + fee; the limit policy sees the balance *after*; a rejected withdrawal changes nothing.
 */
public final class Account {

    private final String id;
    private final Product product;
    private final String currency;
    private Money balance; // the only mutable state; always replaced, never modified (Money is immutable)

    public Account(String id, Product product, String currency) {
        this.id = Objects.requireNonNull(id, "id");
        this.product = Objects.requireNonNull(product, "product");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.balance = Money.zero(currency);
    }

    public void deposit(Money amount) {
        requirePositive(amount);
        balance = balance.plus(amount);
    }

    public void withdraw(Money amount) {
        requirePositive(amount);
        Money fee = product.fees().withdrawalFee(amount);
        Money after = balance.minus(amount).minus(fee);
        product.limits().checkWithdrawal(amount, after); // throws before anything is assigned
        balance = after;
    }

    public void applyMonthlyInterest() {
        balance = balance.plus(product.interest().monthlyInterest(balance));
    }

    public String id() { return id; }

    public Product product() { return product; }

    public Money balance() { return balance; }

    private void requirePositive(Money amount) {
        Objects.requireNonNull(amount, "amount");
        if (!amount.currency().equals(currency)) {
            throw new IllegalArgumentException("Account " + id + " is in " + currency + ", not " + amount.currency());
        }
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be positive: " + amount);
        }
    }
}
