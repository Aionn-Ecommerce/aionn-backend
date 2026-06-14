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
