package dev.aqib.payments.ledger.domain;

/**
 * The product catalogue. Each product is a combination of policies (see AccountTest for the exact numbers).
 *  - EASY_SAVER: 4.5% a year, no fees, no overdraft, max £5,000 per withdrawal
 *  - CURRENT: no interest, no fees, overdraft up to £500
 *  - PREMIUM_CURRENT: 1% a year, fee 0.5% per withdrawal (min £1.00), overdraft up to £2,000
 */
public final class Products {
    private Products() {}

    public static Product easySaver() { throw new UnsupportedOperationException("TODO"); }

    public static Product currentAccount() { throw new UnsupportedOperationException("TODO"); }

    public static Product premiumCurrent() { throw new UnsupportedOperationException("TODO"); }
}
