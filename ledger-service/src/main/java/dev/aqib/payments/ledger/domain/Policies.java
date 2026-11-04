package dev.aqib.payments.ledger.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;

/**
 * Small, composable behaviours. Each one is a few lines; most can be a lambda.
 * The point of the lesson: products are combinations of these, not subclasses.
 */
public final class Policies {
    private Policies() {}

    private static final BigDecimal MONTHS_TIMES_100 = BigDecimal.valueOf(1200);

    /** Always zero, in the given currency. */
    public static InterestPolicy noInterest(String currency) {
        Money zero = Money.zero(currency);
        return balance -> zero;
    }

    /** annualRatePercent 4.5 = 4.5% a year, paid monthly (÷ 12), only on positive balances. */
    public static InterestPolicy fixedRate(BigDecimal annualRatePercent) {
        Objects.requireNonNull(annualRatePercent);
        // 4.5 / 1200 = 0.00375 exactly; 1 / 1200 doesn't terminate, so keep 34 significant digits and round once, in Money.times.
        BigDecimal monthlyFactor = annualRatePercent.divide(MONTHS_TIMES_100, MathContext.DECIMAL128);
        return balance -> balance.isPositive() ? balance.times(monthlyFactor) : Money.zero(balance.currency());
    }

    public static FeePolicy noFee() {
        return amount -> Money.zero(amount.currency());
    }

    public static FeePolicy flatFee(Money fee) {
        Objects.requireNonNull(fee);
        return amount -> fee;
    }

    /** percent of the amount, never less than minimum. */
    public static FeePolicy percentageFee(BigDecimal percent, Money minimum) {
        Objects.requireNonNull(percent);
        Objects.requireNonNull(minimum);
        BigDecimal factor = percent.movePointLeft(2); // 0.5 -> 0.005, exact
        return amount -> {
            Money fee = amount.times(factor);
            return fee.isGreaterThan(minimum) ? fee : minimum;
        };
    }

    /** Balance after the withdrawal may not go below zero. */
    public static LimitPolicy noOverdraft() {
        return (amount, balanceAfter) -> {
            if (balanceAfter.isNegative()) {
                throw new LimitExceededException("No overdraft: balance would be " + balanceAfter);
            }
        };
    }

    /** Balance after the withdrawal may not go below -limit. */
    public static LimitPolicy overdraftUpTo(Money limit) {
        Objects.requireNonNull(limit);
        return (amount, balanceAfter) -> {
            if (balanceAfter.plus(limit).isNegative()) {
                throw new LimitExceededException("Overdraft limit " + limit + " exceeded: balance would be " + balanceAfter);
            }
        };
    }

    /** A single withdrawal may not be bigger than max. */
    public static LimitPolicy maxPerWithdrawal(Money max) {
        Objects.requireNonNull(max);
        return (amount, balanceAfter) -> {
            if (amount.isGreaterThan(max)) {
                throw new LimitExceededException("Max per withdrawal is " + max + ", asked for " + amount);
            }
        };
    }

    /** Every policy must pass. */
    public static LimitPolicy allOf(LimitPolicy... policies) {
        List<LimitPolicy> all = List.of(policies); // defensive copy; also rejects nulls
        return (amount, balanceAfter) -> all.forEach(p -> p.checkWithdrawal(amount, balanceAfter));
    }
}
