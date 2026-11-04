package dev.aqib.payments.ledger.domain;

import java.math.BigDecimal;

/**
 * The product catalogue. Each product is a combination of policies (see AccountTest for the exact numbers).
 *  - EASY_SAVER: 4.5% a year, no fees, no overdraft, max £5,000 per withdrawal
 *  - CURRENT: no interest, no fees, overdraft up to £500
 *  - PREMIUM_CURRENT: 1% a year, fee 0.5% per withdrawal (min £1.00), overdraft up to £2,000
 */
public final class Products {
    private Products() {}

    private static Money gbp(String amount) { return Money.of(amount, "GBP"); }

    public static Product easySaver() {
        return new Product("EASY_SAVER",
                Policies.fixedRate(new BigDecimal("4.5")),
                Policies.noFee(),
                Policies.allOf(Policies.noOverdraft(), Policies.maxPerWithdrawal(gbp("5000.00"))));
    }

    public static Product currentAccount() {
        return new Product("CURRENT",
                Policies.noInterest("GBP"),
                Policies.noFee(),
                Policies.overdraftUpTo(gbp("500.00")));
    }

    public static Product premiumCurrent() {
        return new Product("PREMIUM_CURRENT",
                Policies.fixedRate(new BigDecimal("1")),
                Policies.percentageFee(new BigDecimal("0.5"), gbp("1.00")),
                Policies.overdraftUpTo(gbp("2000.00")));
    }
}
