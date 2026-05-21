-- =====================================================================
-- PROMOTION MODULE - INIT SCHEMA
-- =====================================================================

CREATE TABLE promotion_campaigns (
    campaign_id            VARCHAR(50) PRIMARY KEY,
    name                   VARCHAR(150) NOT NULL,
    type                   VARCHAR(20) NOT NULL,
    budget                 NUMERIC(18,2) NOT NULL,
    budget_remaining       NUMERIC(18,2) NOT NULL,
    currency               VARCHAR(3)  NOT NULL,
    start_date             TIMESTAMPTZ NOT NULL,
    end_date               TIMESTAMPTZ NOT NULL,
    created_by             VARCHAR(50),
    status                 VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    min_order_value        NUMERIC(18,2),
    applicable_categories  JSONB,
    max_claims_per_user    INT,
    max_uses_per_voucher   INT,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_campaign_dates CHECK (start_date < end_date),
    CONSTRAINT chk_campaign_remaining CHECK (budget_remaining >= 0 AND budget_remaining <= budget)
);
CREATE INDEX idx_campaigns_status_dates ON promotion_campaigns(status, start_date, end_date);

CREATE TABLE vouchers (
    voucher_code    VARCHAR(50) PRIMARY KEY,
    campaign_id     VARCHAR(50) NOT NULL,
    discount_amount NUMERIC(18,2) NOT NULL,
    currency        VARCHAR(3)  NOT NULL,
    usage_limit     INT NOT NULL,
    used_count      INT NOT NULL DEFAULT 0,
    reserved_count  INT NOT NULL DEFAULT 0,
    valid_from      TIMESTAMPTZ,
    valid_until     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_vouchers_campaign FOREIGN KEY (campaign_id) REFERENCES promotion_campaigns(campaign_id),
    CONSTRAINT chk_vouchers_counts CHECK (used_count >= 0 AND reserved_count >= 0
                                          AND (used_count + reserved_count) <= usage_limit)
);
CREATE INDEX idx_vouchers_campaign ON vouchers(campaign_id);

CREATE TABLE user_vouchers (
    user_voucher_id      VARCHAR(50) PRIMARY KEY,
    voucher_code         VARCHAR(50) NOT NULL,
    user_id              VARCHAR(50) NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'CLAIMED',
    reserved_order_id    VARCHAR(50),
    applied_amount       NUMERIC(18,2),
    applied_currency     VARCHAR(3),
    claimed_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reserved_at          TIMESTAMPTZ,
    reserved_expires_at  TIMESTAMPTZ,
    applied_at           TIMESTAMPTZ,
    released_at          TIMESTAMPTZ,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_vouchers_voucher FOREIGN KEY (voucher_code) REFERENCES vouchers(voucher_code)
);
CREATE INDEX idx_user_vouchers_user                ON user_vouchers(user_id);
CREATE UNIQUE INDEX idx_user_vouchers_user_voucher ON user_vouchers(user_id, voucher_code);
CREATE INDEX idx_user_vouchers_status_expires      ON user_vouchers(status, reserved_expires_at);
CREATE INDEX idx_user_vouchers_order               ON user_vouchers(reserved_order_id);
