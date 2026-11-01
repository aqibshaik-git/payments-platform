package dev.aqib.payments.ledger.domain;

import java.math.BigDecimal;

/**
 * Small, composable behaviours. Each one is a few lines; most can be a lambda.
 * The point of the lesson: products are combinations of these, not subclasses.
 */
public final class Policies {
    private Policies() {}

    /** Always zero, in the given currency. */
    public static InterestPolicy noInterest(String currency) { throw new UnsupportedOperationException("TODO"); }

    /** annualRatePercent 4.5 = 4.5% a year, paid monthly (÷ 12), only on positive balances. */
    public static InterestPolicy fixedRate(BigDecimal annualRatePercent) { throw new UnsupportedOperationException("TODO"); }

    public static FeePolicy noFee() { throw new UnsupportedOperationException("TODO"); }

    public static FeePolicy flatFee(Money fee) { throw new UnsupportedOperationException("TODO"); }

    /** percent of the amount, never less than minimum. */
    public static FeePolicy percentageFee(BigDecimal percent, Money minimum) { throw new UnsupportedOperationException("TODO"); }

    /** Balance after the withdrawal may not go below zero. */
    public static LimitPolicy noOverdraft() { throw new UnsupportedOperationException("TODO"); }

    /** Balance after the withdrawal may not go below -limit. */
    public static LimitPolicy overdraftUpTo(Money limit) { throw new UnsupportedOperationException("TODO"); }

    /** A single withdrawal may not be bigger than max. */
    public static LimitPolicy maxPerWithdrawal(Money max) { throw new UnsupportedOperationException("TODO"); }

    /** Every policy must pass. */
    public static LimitPolicy allOf(LimitPolicy... policies) { throw new UnsupportedOperationException("TODO"); }
}
