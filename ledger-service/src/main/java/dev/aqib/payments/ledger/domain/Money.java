package dev.aqib.payments.ledger.domain;

import java.math.BigDecimal;

/**
 * An amount of money in one currency. Immutable: every operation returns a new Money.
 *
 * Rules (MoneyTest checks all of them):
 *  - never a double: BigDecimal, scaled to the currency's decimal places (GBP 2, JPY 0)
 *  - "10" and "10.00" are the same money; equals and hashCode must agree
 *  - more decimal places than the currency allows is an error (IllegalArgumentException)
 *  - mixing currencies is an error (IllegalArgumentException)
 *  - times() rounds HALF_EVEN (banker's rounding)
 */
public final class Money implements Comparable<Money> {

    // TODO: fields. Think about which types make illegal states impossible.

    public static Money of(String amount, String currencyCode) {
        throw new UnsupportedOperationException("TODO");
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        throw new UnsupportedOperationException("TODO");
    }

    public static Money zero(String currencyCode) {
        throw new UnsupportedOperationException("TODO");
    }

    public Money plus(Money other) { throw new UnsupportedOperationException("TODO"); }

    public Money minus(Money other) { throw new UnsupportedOperationException("TODO"); }

    public Money times(BigDecimal factor) { throw new UnsupportedOperationException("TODO"); }

    public boolean isNegative() { throw new UnsupportedOperationException("TODO"); }

    public boolean isPositive() { throw new UnsupportedOperationException("TODO"); }

    public boolean isGreaterThan(Money other) { throw new UnsupportedOperationException("TODO"); }

    public BigDecimal amount() { throw new UnsupportedOperationException("TODO"); }

    public String currency() { throw new UnsupportedOperationException("TODO"); }

    @Override public int compareTo(Money other) { throw new UnsupportedOperationException("TODO"); }

    // TODO: equals, hashCode, toString ("GBP 10.00", "JPY 100")
}
