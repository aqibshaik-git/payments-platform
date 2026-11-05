package dev.aqib.payments.ledger.events;

import dev.aqib.payments.ledger.domain.Money;

/**
 * Mon 9 Nov · the ledger's events: a CLOSED set of shapes, so a sealed interface plus records.
 * Given: the three shapes. You write: {@link AccountProjection} and {@link IdempotencyStore}.
 * Lesson: ~/dev/learning/2026-11-09-java-collections-generics/README.md
 */
public sealed interface LedgerEvent permits LedgerEvent.Posted, LedgerEvent.Reversed, LedgerEvent.Held {

    String eventId();

    /** Money moved: positive = credit, negative = debit. */
    record Posted(String eventId, String accountId, Money amount) implements LedgerEvent {}

    /** Money reserved (e.g. a card authorisation): lowers what's available, not the ledger balance. */
    record Held(String eventId, String accountId, Money amount) implements LedgerEvent {}

    /** Undo an earlier Posted or Held, identified by its eventId. */
    record Reversed(String eventId, String originalEventId, String reason) implements LedgerEvent {}
}
