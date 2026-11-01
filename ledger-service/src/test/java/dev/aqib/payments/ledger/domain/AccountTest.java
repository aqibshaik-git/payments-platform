package dev.aqib.payments.ledger.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AccountTest {

    private static Money gbp(String s) { return Money.of(s, "GBP"); }

    @Test void depositAndWithdraw() {
        Account a = new Account("A1", Products.currentAccount(), "GBP");
        a.deposit(gbp("100.00"));
        a.withdraw(gbp("30.00"));
        assertEquals(gbp("70.00"), a.balance());
    }

    @Test void savingsCannotGoOverdrawn() {
        Account a = new Account("A2", Products.easySaver(), "GBP");
        a.deposit(gbp("50.00"));
        assertThrows(LimitExceededException.class, () -> a.withdraw(gbp("50.01")));
        assertEquals(gbp("50.00"), a.balance(), "a rejected withdrawal must not change the balance");
    }

    @Test void currentAccountOverdraftHasALimit() {
        Account a = new Account("A3", Products.currentAccount(), "GBP");
        a.withdraw(gbp("500.00"));
        assertEquals(gbp("-500.00"), a.balance());
        assertThrows(LimitExceededException.class, () -> a.withdraw(gbp("0.01")));
    }

    @Test void savingsHasAMaxSingleWithdrawal() {
        Account a = new Account("A4", Products.easySaver(), "GBP");
        a.deposit(gbp("10000.00"));
        assertThrows(LimitExceededException.class, () -> a.withdraw(gbp("5000.01")));
        a.withdraw(gbp("5000.00"));
        assertEquals(gbp("5000.00"), a.balance());
    }

    @Test void monthlyInterestOnSavings() {
        Account a = new Account("A5", Products.easySaver(), "GBP");
        a.deposit(gbp("1200.00"));
        a.applyMonthlyInterest(); // 4.5% a year = 0.375% a month = 4.50
        assertEquals(gbp("1204.50"), a.balance());
    }

    @Test void noInterestOnNegativeBalance() {
        Account a = new Account("A6", Products.premiumCurrent(), "GBP");
        a.withdraw(gbp("100.00")); // fee 0.5% of 100 = 0.50, but minimum 1.00
        assertEquals(gbp("-101.00"), a.balance());
        a.applyMonthlyInterest();
        assertEquals(gbp("-101.00"), a.balance());
    }

    @Test void percentageFeeAboveTheMinimum() {
        Account a = new Account("A7", Products.premiumCurrent(), "GBP");
        a.deposit(gbp("1000.00"));
        a.withdraw(gbp("400.00")); // 0.5% = 2.00
        assertEquals(gbp("598.00"), a.balance());
    }

    @Test void depositsMustBePositive() {
        Account a = new Account("A8", Products.currentAccount(), "GBP");
        assertThrows(IllegalArgumentException.class, () -> a.deposit(gbp("0.00")));
        assertThrows(IllegalArgumentException.class, () -> a.withdraw(gbp("-1.00")));
    }

    @Test void aNewProductIsJustANewCombination() {
        Product student = new Product("STUDENT", Policies.noInterest("GBP"), Policies.flatFee(gbp("0.25")),
                Policies.allOf(Policies.overdraftUpTo(gbp("1000.00")), Policies.maxPerWithdrawal(gbp("300.00"))));
        Account a = new Account("A9", student, "GBP");
        a.withdraw(gbp("100.00"));
        assertEquals(gbp("-100.25"), a.balance());
        assertThrows(LimitExceededException.class, () -> a.withdraw(gbp("300.01")));
    }
}
