-- Tue 10 Nov · YOU write these three functions. Same signatures as the naive ones.
-- Lesson: ~/dev/learning/2026-11-10-transactions-isolation/README.md, section 5.

-- 1. Pessimistic: lock both rows before reading. Lock them in id order. (Why? Try it the other way.)
CREATE OR REPLACE FUNCTION tx.transfer_locked(src int, dst int, amt bigint) RETURNS void AS $$
BEGIN
  RAISE EXCEPTION 'TODO';
END $$ LANGUAGE plpgsql;

-- 2. Optimistic: read balance + version, then UPDATE ... WHERE version = the one you read.
--    0 rows updated means someone got there first: return false and let the caller retry.
--    Both rows must change or neither (the function runs in one transaction; raise to roll back).
CREATE OR REPLACE FUNCTION tx.transfer_optimistic(src int, dst int, amt bigint) RETURNS boolean AS $$
BEGIN
  RAISE EXCEPTION 'TODO';
END $$ LANGUAGE plpgsql;

-- 3. Write skew, fixed WITHOUT changing the isolation level: make the check-then-debit safe
--    by locking what the rule depends on.
CREATE OR REPLACE FUNCTION tx.withdraw_safe(acct int, amt bigint) RETURNS void AS $$
BEGIN
  RAISE EXCEPTION 'TODO';
END $$ LANGUAGE plpgsql;
