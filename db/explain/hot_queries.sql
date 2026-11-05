-- Sun 15 Nov · the ledger's three hottest queries. Don't change these; change the indexes.
-- Q1 · statement page: an account's latest 20 postings (runs on every app open)
SELECT id, amount_minor, posted_at FROM ledger.postings WHERE account_id = 1234 ORDER BY posted_at DESC, id DESC LIMIT 20;
-- Q2 · transfer detail: all postings of one transfer (support screens, reconciliation)
SELECT account_id, amount_minor FROM ledger.postings WHERE transfer_id = 54321;
-- Q3 · daily balance check: total movement per account for one day (nightly job)
SELECT account_id, sum(amount_minor) FROM ledger.postings
WHERE posted_at >= '2026-08-15' AND posted_at < '2026-08-16' GROUP BY account_id;
