-- =====================================================================
-- PAYMENT MODULE - OPTIMISTIC LOCK VERSION COLUMNS
-- payments already has a version column shipped in V1_6.
-- transaction_ledgers is append-only, no version needed.
-- This patch only adds version to payment_methods.
-- =====================================================================

ALTER TABLE payment_methods ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
