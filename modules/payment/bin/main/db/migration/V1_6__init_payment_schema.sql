-- PAYMENT MODULE - COMPLETE INIT SCHEMA

CREATE TABLE payment_methods (
    method_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    provider VARCHAR(50),
    last_4_digits VARCHAR(4),
    status VARCHAR(20) DEFAULT 'LINKED',
    verified_at TIMESTAMP
);

CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2),
    gateway VARCHAR(50),
    transaction_no VARCHAR(100),
    status VARCHAR(20) DEFAULT 'INITIATED',
    invoice_url TEXT,
    paid_at TIMESTAMP
);

CREATE TABLE transaction_ledgers (
    ledger_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50),
    amount DECIMAL(15,2),
    type VARCHAR(20),
    gateway VARCHAR(50),
    status VARCHAR(20) DEFAULT 'RECORDED',
    reconciled_at TIMESTAMP
);

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
