package dev.aqib.payments.ledger.domain;

/** What a withdrawal costs. */
@FunctionalInterface
public interface FeePolicy {
    Money withdrawalFee(Money amount);
}
