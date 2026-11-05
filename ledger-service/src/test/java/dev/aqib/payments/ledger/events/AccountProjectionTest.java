package dev.aqib.payments.ledger.events;

import dev.aqib.payments.ledger.domain.Money;
import dev.aqib.payments.ledger.events.LedgerEvent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountProjectionTest {
    static Money gbp(String a) { return Money.of(a, "GBP"); }

    @Test void postedMovesBothNumbers() {
        var p = new AccountProjection();
        p.apply(new Posted("e1", "A", gbp("100.00")));
        p.apply(new Posted("e2", "A", gbp("-30.00")));
        assertEquals(gbp("70.00"), p.ledgerBalance("A"));
        assertEquals(gbp("70.00"), p.available("A"));
    }

    @Test void holdLowersAvailableOnly() {
        var p = new AccountProjection();
        p.apply(new Posted("e1", "A", gbp("100.00")));
        p.apply(new Held("h1", "A", gbp("40.00")));
        assertEquals(gbp("100.00"), p.ledgerBalance("A"));
        assertEquals(gbp("60.00"), p.available("A"));
    }

    @Test void reversingAHoldReleasesIt() {
        var p = new AccountProjection();
        p.apply(new Posted("e1", "A", gbp("100.00")));
        p.apply(new Held("h1", "A", gbp("40.00")));
        p.apply(new Reversed("r1", "h1", "auth expired"));
        assertEquals(gbp("100.00"), p.available("A"));
        assertEquals(gbp("100.00"), p.ledgerBalance("A"));
    }

    @Test void reversingAPostingTakesItBack() {
        var p = new AccountProjection();
        p.apply(new Posted("e1", "A", gbp("100.00")));
        p.apply(new Posted("e2", "A", gbp("-25.00")));
        p.apply(new Reversed("r1", "e2", "duplicate charge"));
        assertEquals(gbp("100.00"), p.ledgerBalance("A"));
        assertEquals(gbp("100.00"), p.available("A"));
    }

    @Test void duplicateEventIdIsIgnored() {
        var p = new AccountProjection();
        assertTrue(p.apply(new Posted("e1", "A", gbp("10.00"))));
        assertFalse(p.apply(new Posted("e1", "A", gbp("10.00"))));
        assertEquals(gbp("10.00"), p.ledgerBalance("A"));
    }

    @Test void reversingUnknownOrAReversalIsRejected() {
        var p = new AccountProjection();
        p.apply(new Posted("e1", "A", gbp("10.00")));
        p.apply(new Reversed("r1", "e1", "x"));
        assertThrows(IllegalArgumentException.class, () -> p.apply(new Reversed("r2", "nope", "x")));
        assertThrows(IllegalArgumentException.class, () -> p.apply(new Reversed("r3", "r1", "x")));
    }

    @Test void unknownAccountIsZero() {
        var p = new AccountProjection();
        assertEquals(Money.zero("GBP"), p.ledgerBalance("Z"));
        assertEquals(Money.zero("GBP"), p.available("Z"));
    }
}
