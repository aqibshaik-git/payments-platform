-- Sun 8 Nov · the ledger schema. YOU write this file.
-- Lesson: ~/dev/learning/2026-11-08-sql-in-depth/README.md, section 4.
--
-- The contract the seed, the fixture and the tests rely on (names and types must match):
--
--   accounts  (id bigint PK, currency char(3) NOT NULL, opened_at timestamptz NOT NULL)
--   transfers (id bigint PK, idempotency_key text NOT NULL UNIQUE, created_at timestamptz NOT NULL)
--   postings  (id bigint PK generated always as identity,
--              transfer_id bigint NOT NULL -> transfers,
--              account_id  bigint NOT NULL,
--              currency    char(3) NOT NULL,
--              amount_minor bigint NOT NULL,          -- pence; negative = debit, positive = credit
--              posted_at   timestamptz NOT NULL)
--
-- Rules the database itself must enforce (tests try to break each one):
--   1. amount_minor is never 0
--   2. a posting's currency must equal its account's currency     (hint: a composite foreign key)
--   3. every transfer's postings sum to exactly 0, checked at COMMIT, not per row
--                                                                  (hint: a deferred constraint trigger)
--
-- Start every run from a clean slate:
DROP SCHEMA IF EXISTS ledger CASCADE;
CREATE SCHEMA ledger;
SET search_path = ledger;

-- TODO: your CREATE TABLE statements, constraints and the balance trigger go here.
