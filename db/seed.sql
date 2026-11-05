-- 100,000 balanced transfers between 2,000 GBP accounts over 90 days (200,000 postings).
-- Run after schema.sql:  psql ... -f schema.sql -f seed.sql
SET search_path = ledger;
INSERT INTO accounts (id, currency, opened_at)
SELECT g, 'GBP', timestamptz '2026-06-01' FROM generate_series(1, 2000) g;

INSERT INTO transfers (id, idempotency_key, created_at)
SELECT g, 'seed-' || g, timestamptz '2026-08-01' + (g % 90) * interval '1 day' + (g % 86400) * interval '1 second'
FROM generate_series(1, 100000) g;

BEGIN;
INSERT INTO postings (transfer_id, account_id, currency, amount_minor, posted_at)
SELECT t.id, a.src, 'GBP', -a.amt, t.created_at
FROM transfers t
CROSS JOIN LATERAL (SELECT 1 + (t.id * 7919) % 2000 AS src,
                           1 + (t.id * 104729 + 13) % 2000 AS dst,
                           100 + (t.id * 31337) % 50000 AS amt) a
UNION ALL
SELECT t.id, CASE WHEN a.dst = a.src THEN a.dst % 2000 + 1 ELSE a.dst END, 'GBP', a.amt, t.created_at
FROM transfers t
CROSS JOIN LATERAL (SELECT 1 + (t.id * 7919) % 2000 AS src,
                           1 + (t.id * 104729 + 13) % 2000 AS dst,
                           100 + (t.id * 31337) % 50000 AS amt) a;
COMMIT;
ANALYZE;
