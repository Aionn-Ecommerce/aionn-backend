-- =====================================================================
-- PROMOTION MODULE - OPTIMISTIC LOCK VERSION COLUMNS
-- vouchers already has a version column shipped in V1_7.
-- =====================================================================

ALTER TABLE promotion_campaigns ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE user_vouchers       ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
