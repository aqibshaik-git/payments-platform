package dev.aqib.payments.ledger.events;

import dev.aqib.payments.ledger.domain.Money;

/**
 * Folds ledger events into two numbers per account:
 *   ledger balance  = sum of Posted (minus reversed ones)
 *   available       = ledger balance minus active holds
 *
 * Rules (each one has a test):
 *  - apply() is idempotent: an eventId already seen is ignored and apply() returns false.
 *  - Reversed of a Posted takes the money back out of both numbers.
 *  - Reversed of a Held gives the held amount back to available (the ledger balance never moved).
 *  - Reversing an unknown event, or reversing a Reversed, throws IllegalArgumentException.
 *  - An account never seen has balance and available of zero GBP.
 *
 * The heart of it is ONE switch over LedgerEvent with no default branch. Delete a case and the
 * compiler should complain. That's the point of sealed.
 */
public final class AccountProjection {

    public boolean apply(LedgerEvent event) {
        throw new UnsupportedOperationException("TODO: you write this");
    }

    public Money ledgerBalance(String accountId) {
        throw new UnsupportedOperationException("TODO");
    }

    public Money available(String accountId) {
        throw new UnsupportedOperationException("TODO");
    }
}
