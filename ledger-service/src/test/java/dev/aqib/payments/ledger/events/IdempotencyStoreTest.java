package dev.aqib.payments.ledger.events;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class IdempotencyStoreTest {

    /** A clock the test moves by hand. */
    static final class TestClock extends Clock {
        Instant now = Instant.parse("2026-11-09T09:00:00Z");
        void advance(Duration d) { now = now.plus(d); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId z) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test void secondCallReturnsStoredResultWithoutWork() {
        var clock = new TestClock();
        var store = new IdempotencyStore<String>(Duration.ofMinutes(10), clock);
        var runs = new AtomicInteger();
        assertEquals("transfer-1", store.getOrCompute("k1", () -> { runs.incrementAndGet(); return "transfer-1"; }));
        assertEquals("transfer-1", store.getOrCompute("k1", () -> { runs.incrementAndGet(); return "transfer-2"; }));
        assertEquals(1, runs.get());
    }

    @Test void afterTtlTheKeyIsAbsentAgain() {
        var clock = new TestClock();
        var store = new IdempotencyStore<String>(Duration.ofMinutes(10), clock);
        store.getOrCompute("k1", () -> "first");
        clock.advance(Duration.ofMinutes(11));
        assertTrue(store.peek("k1").isEmpty());
        assertEquals("second", store.getOrCompute("k1", () -> "second"));
    }

    @Test void peekNeverRunsWork() {
        var store = new IdempotencyStore<String>(Duration.ofMinutes(10), new TestClock());
        assertTrue(store.peek("nope").isEmpty());
        store.getOrCompute("k1", () -> "v");
        assertEquals("v", store.peek("k1").orElseThrow());
    }

    @Test void purgeRemovesOnlyExpired() {
        var clock = new TestClock();
        var store = new IdempotencyStore<String>(Duration.ofMinutes(10), clock);
        store.getOrCompute("old1", () -> "a");
        store.getOrCompute("old2", () -> "b");
        clock.advance(Duration.ofMinutes(8));
        store.getOrCompute("new", () -> "c");
        clock.advance(Duration.ofMinutes(5));
        assertEquals(2, store.purgeExpired());
        assertEquals("c", store.peek("new").orElseThrow());
    }

    @Test void sameKeyFromManyThreadsRunsWorkOnce() throws Exception {
        var store = new IdempotencyStore<Integer>(Duration.ofMinutes(10), new TestClock());
        var runs = new AtomicInteger();
        var start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(16)) {
            var futures = new java.util.ArrayList<Future<Integer>>();
            for (int i = 0; i < 64; i++) futures.add(pool.submit(() -> {
                start.await();
                return store.getOrCompute("same", () -> { runs.incrementAndGet(); sleep(20); return 42; });
            }));
            start.countDown();
            for (var f : futures) assertEquals(42, f.get());
        }
        assertEquals(1, runs.get());
    }

    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}
