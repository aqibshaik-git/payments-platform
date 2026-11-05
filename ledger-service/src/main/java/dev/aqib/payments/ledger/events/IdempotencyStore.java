package dev.aqib.payments.ledger.events;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Remembers the result of work done for an idempotency key, for a limited time.
 * The transfer API will use it: the same request retried with the same key gets the same answer,
 * and the transfer happens once.
 *
 * Rules (each one has a test):
 *  - getOrCompute(key, work): the first call runs work and stores the result; later calls within
 *    the TTL return the stored result without running work.
 *  - After the TTL, the key counts as absent: work runs again.
 *  - Many threads calling getOrCompute with the SAME key at once: work runs exactly once.
 *  - peek(key) never runs work; it returns empty for missing or expired keys.
 *  - purgeExpired() removes expired entries and returns how many it removed.
 *  - Time comes only from the injected Clock (tests move it by hand). Never System.currentTimeMillis().
 */
public final class IdempotencyStore<V> {

    public IdempotencyStore(Duration ttl, Clock clock) {
        throw new UnsupportedOperationException("TODO: you write this");
    }

    public V getOrCompute(String key, Supplier<V> work) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<V> peek(String key) {
        throw new UnsupportedOperationException("TODO");
    }

    public int purgeExpired() {
        throw new UnsupportedOperationException("TODO");
    }
}
