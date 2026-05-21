-- =====================================================================
-- NOTIFICATION MODULE - INIT SCHEMA
-- =====================================================================

CREATE TABLE notification_templates (
    template_id  VARCHAR(50) PRIMARY KEY,
    event_type   VARCHAR(100) NOT NULL,
    channel      VARCHAR(20)  NOT NULL,
    category     VARCHAR(20)  NOT NULL,
    locale       VARCHAR(20)  NOT NULL DEFAULT 'vi-VN',
    subject      TEXT,
    content      TEXT NOT NULL,
    placeholders JSONB NOT NULL DEFAULT '[]'::jsonb,
    version      INT  NOT NULL DEFAULT 1,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX idx_notification_templates_event_channel_locale
    ON notification_templates(event_type, channel, locale);

CREATE TABLE notifications (
    noti_id              VARCHAR(50) PRIMARY KEY,
    user_id              VARCHAR(50) NOT NULL,
    template_id          VARCHAR(50),
    channel              VARCHAR(20) NOT NULL,
    category             VARCHAR(20) NOT NULL,
    priority             VARCHAR(20) NOT NULL,
    subject              TEXT,
    content              TEXT NOT NULL,
    campaign_id          VARCHAR(50),
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count          INT NOT NULL DEFAULT 0,
    last_failure_reason  TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at              TIMESTAMPTZ,
    read_at              TIMESTAMPTZ,
    deleted_at           TIMESTAMPTZ
);
CREATE INDEX idx_notifications_user_status     ON notifications(user_id, status);
CREATE INDEX idx_notifications_campaign_status ON notifications(campaign_id, status);
CREATE INDEX idx_notifications_status_retry    ON notifications(status, retry_count);

CREATE TABLE device_tokens (
    token_id      VARCHAR(50) PRIMARY KEY,
    user_id       VARCHAR(50) NOT NULL,
    device_token  VARCHAR(512) NOT NULL,
    os            VARCHAR(20),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_device_tokens_user ON device_tokens(user_id);
CREATE UNIQUE INDEX idx_device_tokens_user_token ON device_tokens(user_id, device_token);

CREATE TABLE notification_subscriptions (
    user_id    VARCHAR(50) PRIMARY KEY,
    settings   JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE notification_configurations (
    provider_id           VARCHAR(50) PRIMARY KEY,
    channel               VARCHAR(20) NOT NULL,
    provider_type         VARCHAR(50) NOT NULL,
    config                JSONB NOT NULL DEFAULT '{}'::jsonb,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    rate_limit_per_minute INT NOT NULL DEFAULT 60,
    configured_by         VARCHAR(50),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_provider_channel_active ON notification_configurations(channel, is_active);

-- analytics is computed on-demand; no separate table needed for now.
