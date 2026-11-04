package dev.aqib.payments.ledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * An amount of money in one currency. Immutable: every operation returns a new Money.
 *
 * Rules (MoneyTest checks all of them):
 *  - never a double: BigDecimal, scaled to the currency's decimal places (GBP 2, JPY 0)
 *  - "10" and "10.00" are the same money; equals and hashCode must agree
 *  - more decimal places than the currency allows is an error (IllegalArgumentException)
 *  - mixing currencies is an error (IllegalArgumentException)
 *  - times() rounds HALF_EVEN (banker's rounding)
 *
 * Why these field types: java.util.Currency, not a String, so "GBX" or "gbp" can't sneak in;
 * the amount is always stored at the currency's scale, so 10 and 10.00 become the same BigDecimal
 * and plain BigDecimal.equals/hashCode then agree (BigDecimal.equals alone treats 10 and 10.00 as different).
 */
public final class Money implements Comparable<Money> {

    private final BigDecimal amount;   // always at currency.getDefaultFractionDigits() scale
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(String amount, String currencyCode) {
        Objects.requireNonNull(amount, "amount");
        return of(new BigDecimal(amount), currencyCode);
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        Objects.requireNonNull(amount, "amount");
        Currency currency = currencyOf(currencyCode);
        int digits = currency.getDefaultFractionDigits();
        // Reject 1.005 GBP instead of silently rounding: losing a fraction of a penny is a bug, not a rounding choice.
        if (amount.stripTrailingZeros().scale() > digits) {
            throw new IllegalArgumentException(
                    amount.toPlainString() + " has more decimal places than " + currency.getCurrencyCode() + " allows (" + digits + ")");
        }
        return new Money(amount.setScale(digits, RoundingMode.UNNECESSARY), currency);
    }

    public static Money zero(String currencyCode) {
        return of(BigDecimal.ZERO, currencyCode);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    /** The one place rounding is allowed: a factor (rate, percentage) can produce sub-penny results. */
    public Money times(BigDecimal factor) {
        Objects.requireNonNull(factor, "factor");
        return new Money(amount.multiply(factor).setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN), currency);
    }

    public boolean isNegative() { return amount.signum() < 0; }

    public boolean isPositive() { return amount.signum() > 0; }

    public boolean isGreaterThan(Money other) { return compareTo(other) > 0; }

    public BigDecimal amount() { return amount; }

    public String currency() { return currency.getCurrencyCode(); }

    @Override public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Money m && currency.equals(m.currency) && amount.equals(m.amount);
    }

    @Override public int hashCode() { return Objects.hash(amount, currency); }

    @Override public String toString() { return currency.getCurrencyCode() + " " + amount.toPlainString(); }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency.getCurrencyCode() + " vs " + other.currency.getCurrencyCode());
        }
    }

    private static Currency currencyOf(String code) {
        Objects.requireNonNull(code, "currencyCode");
        try {
            return Currency.getInstance(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown currency: " + code, e);
        }
    }
}
