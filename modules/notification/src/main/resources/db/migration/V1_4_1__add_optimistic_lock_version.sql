-- =====================================================================
-- NOTIFICATION MODULE - OPTIMISTIC LOCK VERSION COLUMNS
-- notification_templates already owns a business "version" int column
-- (template revision); no @Version mapped on that entity.
-- =====================================================================

ALTER TABLE notifications                ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE device_tokens                ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE notification_subscriptions   ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE notification_configurations  ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
