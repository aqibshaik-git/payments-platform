package dev.aqib.payments.ledger.domain;

/** Decides whether a withdrawal is allowed. Throws LimitExceededException if not. */
@FunctionalInterface
public interface LimitPolicy {
    void checkWithdrawal(Money amount, Money balanceAfter);
}
