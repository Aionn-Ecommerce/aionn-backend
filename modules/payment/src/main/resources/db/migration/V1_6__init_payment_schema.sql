-- =====================================================================
-- PAYMENT MODULE - INIT SCHEMA
-- Mirrors JPA entities in com.aionn.payment.infrastructure.persistence.
-- =====================================================================

CREATE TABLE payment_methods (
    method_id      VARCHAR(50) PRIMARY KEY,
    user_id        VARCHAR(50) NOT NULL,
    provider       VARCHAR(50) NOT NULL,
    last_4_digits  VARCHAR(4),
    gateway_token  VARCHAR(255) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'LINKED',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    verified_at    TIMESTAMPTZ
);
CREATE INDEX idx_payment_methods_user ON payment_methods(user_id);

CREATE TABLE payments (
    payment_id        VARCHAR(50) PRIMARY KEY,
    order_id          VARCHAR(50) NOT NULL,
    user_id           VARCHAR(50) NOT NULL,
    payment_method_id VARCHAR(50),
    amount            NUMERIC(18,2) NOT NULL,
    refunded_amount   NUMERIC(18,2) NOT NULL DEFAULT 0,
    currency          VARCHAR(3)  NOT NULL,
    gateway           VARCHAR(20) NOT NULL,
    idempotency_key   VARCHAR(100) NOT NULL,
    transaction_no    VARCHAR(100),
    invoice_url       TEXT,
    error_code        VARCHAR(50),
    error_reason      TEXT,
    status            VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    paid_at           TIMESTAMPTZ,
    failed_at         TIMESTAMPTZ,
    version           BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_payments_refund_le_amount CHECK (refunded_amount <= amount)
);
CREATE INDEX idx_payments_order  ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE UNIQUE INDEX idx_payments_idem ON payments(idempotency_key);

CREATE TABLE transaction_ledgers (
    ledger_id              VARCHAR(50) PRIMARY KEY,
    payment_id             VARCHAR(50) NOT NULL,
    amount                 NUMERIC(18,2) NOT NULL,
    currency               VARCHAR(3)  NOT NULL,
    type                   VARCHAR(10) NOT NULL,
    gateway                VARCHAR(20) NOT NULL,
    gateway_transaction_no VARCHAR(100),
    occurred_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_ledgers_type CHECK (type IN ('CREDIT', 'DEBIT'))
);
CREATE INDEX idx_ledgers_payment      ON transaction_ledgers(payment_id);
CREATE INDEX idx_ledgers_gateway_time ON transaction_ledgers(gateway, occurred_at);
CREATE INDEX idx_ledgers_gateway_txn  ON transaction_ledgers(gateway_transaction_no);

