package dev.aqib.payments.ledger.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test void sameAmountDifferentScaleIsEqual() {
        assertEquals(Money.of("10", "GBP"), Money.of("10.00", "GBP"));
        assertEquals(Money.of("10", "GBP").hashCode(), Money.of("10.00", "GBP").hashCode());
    }

    @Test void differentCurrencyIsNotEqual() {
        assertNotEquals(Money.of("10.00", "GBP"), Money.of("10.00", "EUR"));
    }

    @Test void tooManyDecimalPlacesIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("1.005", "GBP"));
    }

    @Test void plusAndMinusReturnNewValues() {
        Money ten = Money.of("10.00", "GBP");
        Money result = ten.plus(Money.of("2.50", "GBP")).minus(Money.of("0.50", "GBP"));
        assertEquals(Money.of("12.00", "GBP"), result);
        assertEquals(Money.of("10.00", "GBP"), ten, "ten must not change: Money is immutable");
    }

    @Test void mixingCurrenciesFails() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("1.00", "GBP").plus(Money.of("1.00", "EUR")));
    }

    @Test void timesRoundsHalfEven() {
        assertEquals(Money.of("0.02", "GBP"), Money.of("0.05", "GBP").times(new BigDecimal("0.5")));
        assertEquals(Money.of("0.08", "GBP"), Money.of("0.15", "GBP").times(new BigDecimal("0.5")));
    }

    @Test void signChecks() {
        assertTrue(Money.of("-0.01", "GBP").isNegative());
        assertFalse(Money.zero("GBP").isPositive());
        assertTrue(Money.of("5.00", "GBP").isGreaterThan(Money.of("4.99", "GBP")));
    }

    @Test void worksInAHashSet() {
        var set = new java.util.HashSet<Money>();
        set.add(Money.of("1", "GBP"));
        assertTrue(set.contains(Money.of("1.00", "GBP")));
    }

    @Test void yenHasNoDecimals() {
        assertEquals("JPY 100", Money.of("100", "JPY").toString());
        assertThrows(IllegalArgumentException.class, () -> Money.of("100.5", "JPY"));
    }
}
