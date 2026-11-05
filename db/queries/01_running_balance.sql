-- LAB · Running balance for every posting, per account, in posting order (posted_at, then id).
-- Output columns, exactly: account_id, posting_id, posted_at, amount_minor, balance_minor
-- One SELECT. A window function, not a self-join.
SELECT NULL::bigint AS account_id, NULL::bigint AS posting_id, NULL::timestamptz AS posted_at,
       NULL::bigint AS amount_minor, NULL::bigint AS balance_minor
WHERE false;  -- TODO: replace with your query
