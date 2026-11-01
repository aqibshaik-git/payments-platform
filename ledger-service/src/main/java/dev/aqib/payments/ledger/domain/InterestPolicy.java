package dev.aqib.payments.ledger.domain;

/** How much interest an account earns each month. */
@FunctionalInterface
public interface InterestPolicy {
    Money monthlyInterest(Money balance);
}
