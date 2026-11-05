-- Tue 10 Nov · a tiny balances table to break on purpose. Separate from the ledger schema.
DROP SCHEMA IF EXISTS tx CASCADE;
CREATE SCHEMA tx;
CREATE TABLE tx.balances (id int PRIMARY KEY, owner text NOT NULL, balance bigint NOT NULL, version int NOT NULL DEFAULT 0);
-- 10 customers, 2 accounts each, £100.00 in every account (amounts in pence)
INSERT INTO tx.balances (id, owner, balance) SELECT g, 'c' || ((g + 1) / 2), 10000 FROM generate_series(1, 20) g;

-- GIVEN · the bug. Read, compute in the "application", write back. Looks fine, isn't.
CREATE FUNCTION tx.transfer_naive(src int, dst int, amt bigint) RETURNS void AS $$
DECLARE s bigint; d bigint;
BEGIN
  SELECT balance INTO s FROM tx.balances WHERE id = src;
  SELECT balance INTO d FROM tx.balances WHERE id = dst;
  PERFORM pg_sleep(0.001);                      -- the gap a real app has between read and write
  UPDATE tx.balances SET balance = s - amt WHERE id = src;
  UPDATE tx.balances SET balance = d + amt WHERE id = dst;
END $$ LANGUAGE plpgsql;

-- GIVEN · write skew. Rule: a customer's two accounts TOGETHER may not go below zero.
-- Each withdrawal checks the rule, then debits ONE account. Two at once, on different accounts, both pass.
CREATE FUNCTION tx.withdraw_naive(acct int, amt bigint) RETURNS void AS $$
DECLARE who text; total bigint;
BEGIN
  SELECT owner INTO who FROM tx.balances WHERE id = acct;
  SELECT sum(balance) INTO total FROM tx.balances WHERE owner = who;
  PERFORM pg_sleep(0.001);
  IF total - amt >= 0 THEN UPDATE tx.balances SET balance = balance - amt WHERE id = acct; END IF;
END $$ LANGUAGE plpgsql;
