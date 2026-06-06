-- =====================================================================
-- ORDERING MODULE - OPTIMISTIC LOCK VERSION COLUMNS
-- Adds @Version columns to support JPA optimistic locking on aggregates
-- updated by concurrent flows (cart edits, order state transitions,
-- return decisions).
-- =====================================================================

ALTER TABLE carts ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE order_returns ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
