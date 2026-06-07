-- =====================================================================
-- CHAT MODULE - OPTIMISTIC LOCK VERSION COLUMNS
-- =====================================================================

ALTER TABLE chat_conversations          ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE chat_messages               ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE chat_user_blocks            ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE chat_merchant_auto_replies  ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
