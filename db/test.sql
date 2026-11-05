-- Tests for the Sun 8 Nov lab. Run with ./test.sh (it loads schema.sql + fixture.sql first).
\set ON_ERROR_STOP on
SET search_path = ledger;

-- helper: assert a statement fails
CREATE OR REPLACE FUNCTION pg_temp.must_fail(sql text, why text) RETURNS void AS $$
BEGIN
  BEGIN
    EXECUTE sql;
    SET CONSTRAINTS ALL IMMEDIATE;   -- fire deferred checks now
  EXCEPTION WHEN others THEN
    RAISE NOTICE 'PASS  %', why; RETURN;
  END;
  RAISE EXCEPTION 'FAIL  % (the database accepted it)', why;
END $$ LANGUAGE plpgsql;

-- 1-4 · the database enforces the rules
SELECT pg_temp.must_fail($$INSERT INTO transfers VALUES (90,'z1',now()); INSERT INTO postings (transfer_id,account_id,currency,amount_minor,posted_at) VALUES (90,1,'GBP',0,now()), (90,2,'GBP',0,now())$$, 'rule 1: a zero posting is rejected');
SELECT pg_temp.must_fail($$INSERT INTO transfers VALUES (91,'z2',now()); INSERT INTO postings (transfer_id,account_id,currency,amount_minor,posted_at) VALUES (91,4,'GBP',-100,now()), (91,1,'GBP',100,now())$$, 'rule 2: a GBP posting on a EUR account is rejected');
SELECT pg_temp.must_fail($$INSERT INTO transfers VALUES (92,'z3',now()); INSERT INTO postings (transfer_id,account_id,currency,amount_minor,posted_at) VALUES (92,1,'GBP',-100,now()), (92,2,'GBP',90,now())$$, 'rule 3: an unbalanced transfer is rejected');
SELECT pg_temp.must_fail($$INSERT INTO transfers VALUES (93,'t10',now())$$, 'idempotency keys are unique');

-- 5 · a balanced transfer written one row at a time is fine (the check waits for COMMIT)
BEGIN;
INSERT INTO transfers VALUES (94,'ok1','2026-05-01');
INSERT INTO postings (transfer_id,account_id,currency,amount_minor,posted_at) VALUES (94,1,'GBP',-50,'2026-05-01');
INSERT INTO postings (transfer_id,account_id,currency,amount_minor,posted_at) VALUES (94,2,'GBP', 50,'2026-05-01');
ROLLBACK;  -- keep the fixture clean
SELECT 'PASS  rule 3 is checked at commit, not per row' AS result;

-- query results vs expected (the fixture's answers, worked out by hand)
CREATE OR REPLACE FUNCTION pg_temp.same(q text, expected text, why text) RETURNS void AS $$
DECLARE missing bigint; extra bigint;
BEGIN
  EXECUTE format('SELECT count(*) FROM ((%s) EXCEPT ALL (%s)) d', expected, q) INTO missing;
  EXECUTE format('SELECT count(*) FROM ((%s) EXCEPT ALL (%s)) d', q, expected) INTO extra;
  IF missing = 0 AND extra = 0 THEN RAISE NOTICE 'PASS  %', why;
  ELSE RAISE EXCEPTION 'FAIL  % (% expected rows missing, % unexpected rows)', why, missing, extra; END IF;
END $$ LANGUAGE plpgsql;
