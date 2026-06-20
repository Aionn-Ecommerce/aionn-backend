-- -----------------------------------------------------------------------------
-- Squashed from V11.0__init_ucp_schema.sql
-- -----------------------------------------------------------------------------
-- UCP module schema:
--   ucp_checkout_session       — UCP-side session before/after a real order is placed.
--   ucp_order_event_outbox     — Lifecycle events to push to the platform's webhook.
-- Platform profile caching is in-memory (Caffeine), no table needed.

CREATE TABLE IF NOT EXISTS ucp_checkout_session (
    session_id              VARCHAR(64)     PRIMARY KEY,
    user_id                 VARCHAR(64),
    platform_profile_url    TEXT,
    webhook_url             TEXT,
    status                  VARCHAR(32)     NOT NULL,
    currency                VARCHAR(8)      NOT NULL,
    line_items_json         JSONB           NOT NULL,
    totals_json             JSONB           NOT NULL,
    discounts_json          JSONB,
    order_id                VARCHAR(64),
    continue_url            TEXT,
    created_at              TIMESTAMPTZ     NOT NULL,
    updated_at              TIMESTAMPTZ     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ucp_checkout_session_user
    ON ucp_checkout_session (user_id);
CREATE INDEX IF NOT EXISTS idx_ucp_checkout_session_status_updated
    ON ucp_checkout_session (status, updated_at);
CREATE INDEX IF NOT EXISTS idx_ucp_checkout_session_order
    ON ucp_checkout_session (order_id);

CREATE TABLE IF NOT EXISTS ucp_order_event_outbox (
    event_id          VARCHAR(64)     PRIMARY KEY,
    order_id          VARCHAR(64)     NOT NULL,
    session_id        VARCHAR(64),
    webhook_url       TEXT            NOT NULL,
    event_type        VARCHAR(64)     NOT NULL,
    payload_json      JSONB           NOT NULL,
    status            VARCHAR(32)     NOT NULL,
    attempts          INT             NOT NULL DEFAULT 0,
    last_attempt_at   TIMESTAMPTZ,
    last_error        TEXT,
    created_at        TIMESTAMPTZ     NOT NULL,
    delivered_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_ucp_order_event_outbox_status_created
    ON ucp_order_event_outbox (status, created_at);
CREATE INDEX IF NOT EXISTS idx_ucp_order_event_outbox_order
    ON ucp_order_event_outbox (order_id);

-- -----------------------------------------------------------------------------
-- Squashed from V11.1__add_ucp_cart_capability.sql
-- -----------------------------------------------------------------------------
-- Add UCP cart capability and cart_id linkage in checkout
-- Per spec: https://ucp.dev/specification/cart

-- Cart sessions table for lightweight basket building before checkout
CREATE TABLE IF NOT EXISTS ucp_cart_sessions (
    cart_id             VARCHAR(50)     PRIMARY KEY,
    user_id             VARCHAR(50),
    currency            VARCHAR(3)      NOT NULL,
    line_items_json     TEXT            NOT NULL,
    totals_json         TEXT,
    continue_url        VARCHAR(500),
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cart_user
    ON ucp_cart_sessions (user_id);

-- Add cart_id to checkout sessions for cart-to-checkout conversion
ALTER TABLE ucp_checkout_session
    ADD COLUMN IF NOT EXISTS cart_id VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_ucp_checkout_session_cart
    ON ucp_checkout_session (cart_id);

COMMENT ON TABLE ucp_cart_sessions IS 'UCP cart sessions - lightweight basket building before checkout per UCP spec';
COMMENT ON COLUMN ucp_checkout_session.cart_id IS 'Source cart ID for cart-to-checkout conversion (idempotent)';
