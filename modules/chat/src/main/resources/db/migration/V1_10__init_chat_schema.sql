-- =====================================================================
-- CHAT MODULE - INIT SCHEMA (Shopee-style buyer ↔ merchant messaging)
-- =====================================================================

CREATE TABLE chat_conversations (
    conversation_id        VARCHAR(50) PRIMARY KEY,
    buyer_id               VARCHAR(50) NOT NULL,
    merchant_id            VARCHAR(50) NOT NULL,
    participants           JSONB       NOT NULL,
    last_message_id        VARCHAR(50),
    last_message_preview   TEXT,
    last_message_type      VARCHAR(20),
    last_message_sender_id VARCHAR(50),
    last_message_at        TIMESTAMPTZ,
    is_archived            BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_conversations_buyer       ON chat_conversations(buyer_id);
CREATE INDEX idx_conversations_merchant    ON chat_conversations(merchant_id);
CREATE UNIQUE INDEX idx_conversations_pair ON chat_conversations(buyer_id, merchant_id);
CREATE INDEX idx_conversations_last_msg    ON chat_conversations(last_message_at);

CREATE TABLE chat_messages (
    message_id      VARCHAR(50) PRIMARY KEY,
    conversation_id VARCHAR(50) NOT NULL,
    sender_id       VARCHAR(50) NOT NULL,
    sender_role     VARCHAR(20) NOT NULL,
    type            VARCHAR(20) NOT NULL,
    body            TEXT,
    metadata        JSONB,
    status          VARCHAR(20) NOT NULL,
    delivered_to    JSONB       NOT NULL DEFAULT '[]'::jsonb,
    read_by         JSONB       NOT NULL DEFAULT '[]'::jsonb,
    is_recalled     BOOLEAN     NOT NULL DEFAULT FALSE,
    sent_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_messages_conv_sent ON chat_messages(conversation_id, sent_at);
CREATE INDEX idx_messages_sender    ON chat_messages(sender_id);

CREATE TABLE chat_user_blocks (
    block_id   VARCHAR(50) PRIMARY KEY,
    blocker_id VARCHAR(50) NOT NULL,
    blocked_id VARCHAR(50) NOT NULL,
    reason     TEXT,
    is_active  BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_user_blocks_blocker ON chat_user_blocks(blocker_id);
CREATE UNIQUE INDEX idx_user_blocks_blocker_blocked
    ON chat_user_blocks(blocker_id, blocked_id);

CREATE TABLE chat_merchant_auto_replies (
    merchant_id        VARCHAR(50) PRIMARY KEY,
    is_enabled         BOOLEAN     NOT NULL DEFAULT FALSE,
    greeting           TEXT,
    away_message       TEXT,
    working_hour_start TIME,
    working_hour_end   TIME,
    working_days       JSONB       NOT NULL,
    timezone           VARCHAR(50) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
