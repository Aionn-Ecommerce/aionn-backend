-- =====================================================================
-- SHIPPING MODULE - OPTIMISTIC LOCK VERSION COLUMNS
-- =====================================================================

ALTER TABLE shipments      ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE shipping_rates ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
