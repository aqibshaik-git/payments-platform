-- Sun 15 Nov · YOU write the indexes here, one per hot query (maybe fewer, if one can serve two).
-- For each, write a one-line comment: which query, why these columns in this order, and what it costs on writes.
SET search_path = ledger;
-- CREATE INDEX ...
ANALYZE postings;
