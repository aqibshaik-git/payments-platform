package dev.aqib.payments.ledger.legacy;

import java.math.BigDecimal;

/**
 * The inheritance version: read it, don't extend it. This is what the 21:05 design question is about.
 *
 * It works for three products. Now the business asks for:
 *  1. a STUDENT account: flat £0.25 fee, overdraft £1,000, max £300 per withdrawal
 *  2. an EASY_SAVER with the PREMIUM fee rules for one corporate client
 * Try to add both without copying code or overriding something you don't own. Where does it break, and why?
 */
public final class LegacyAccounts {
    private LegacyAccounts() {}

    public abstract static class BaseAccount {
        protected BigDecimal balance = BigDecimal.ZERO;

        public void deposit(BigDecimal amount) { balance = balance.add(amount); }

        public void withdraw(BigDecimal amount) {
            BigDecimal after = balance.subtract(amount).subtract(fee(amount));
            if (after.compareTo(overdraftLimit().negate()) < 0) throw new IllegalStateException("limit");
            balance = after;
        }

        protected BigDecimal fee(BigDecimal amount) { return BigDecimal.ZERO; }
        protected BigDecimal overdraftLimit() { return BigDecimal.ZERO; }
        public abstract BigDecimal monthlyInterest();
    }

    public static class SavingsAccount extends BaseAccount {
        @Override public void withdraw(BigDecimal amount) {
            if (amount.compareTo(new BigDecimal("5000")) > 0) throw new IllegalStateException("max");
            super.withdraw(amount);
        }
        @Override public BigDecimal monthlyInterest() {
            return balance.signum() > 0 ? balance.multiply(new BigDecimal("0.00375")) : BigDecimal.ZERO;
        }
    }

    public static class CurrentAccount extends BaseAccount {
        @Override protected BigDecimal overdraftLimit() { return new BigDecimal("500"); }
        @Override public BigDecimal monthlyInterest() { return BigDecimal.ZERO; }
    }

    public static class PremiumCurrentAccount extends CurrentAccount {
        @Override protected BigDecimal overdraftLimit() { return new BigDecimal("2000"); }
        @Override protected BigDecimal fee(BigDecimal amount) {
            BigDecimal pct = amount.multiply(new BigDecimal("0.005"));
            return pct.max(BigDecimal.ONE);
        }
        @Override public BigDecimal monthlyInterest() {
            return balance.signum() > 0 ? balance.multiply(new BigDecimal("0.000833")) : BigDecimal.ZERO;
        }
    }
}
