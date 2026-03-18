-- PROMOTION MODULE - COMPLETE INIT SCHEMA

CREATE TABLE promotion_campaigns (
    campaign_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150),
    budget DECIMAL(15,2),
    min_order_value DECIMAL(15,2),
    applicable_categories JSON,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_by VARCHAR(50)
);

CREATE TABLE vouchers (
    voucher_code VARCHAR(50) PRIMARY KEY,
    campaign_id VARCHAR(50) NOT NULL,
    discount_amount DECIMAL(15,2),
    usage_limit INT,
    valid_until TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES promotion_campaigns(campaign_id)
);

CREATE TABLE user_vouchers (
    id SERIAL PRIMARY KEY,
    voucher_code VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    order_id VARCHAR(50),
    status VARCHAR(20) DEFAULT 'CLAIMED',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voucher_code) REFERENCES vouchers(voucher_code)
);
