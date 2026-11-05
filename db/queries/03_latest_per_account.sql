-- DRILL 1 · The latest posting for each account (your CPA model's "last payment" feature).
-- If two postings share the latest posted_at, take the higher id.
-- Output: account_id, posting_id, posted_at, amount_minor
-- Write it twice: with ROW_NUMBER(), then with Postgres's DISTINCT ON. Keep the one you'd ship.
SELECT NULL::bigint AS account_id, NULL::bigint AS posting_id, NULL::timestamptz AS posted_at, NULL::bigint AS amount_minor WHERE false;  -- TODO
