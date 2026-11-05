-- DRILL 3 · Gaps and islands: for each account, every run of consecutive calendar days with at least
-- one posting. A day with no postings breaks the run.
-- Output: account_id, streak_start (date), streak_end (date), days    ordered by account_id, streak_start
SELECT NULL::bigint AS account_id, NULL::date AS streak_start, NULL::date AS streak_end, NULL::int AS days WHERE false;  -- TODO
