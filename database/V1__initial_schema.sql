-- ==============================================================================
-- MODULAR MONOLITH DATABASE SCHEMA
-- Nguyên tắc: Chỉ dùng FOREIGN KEY trong cùng module, giữa các module dùng khóa mềm (VARCHAR)
-- ==============================================================================

-- ==============================================================================
-- MODULE 1: IDENTITY (Quản lý Định danh, Tài khoản & Bảo mật)
-- ==============================================================================

CREATE TABLE registrations (
    reg_id VARCHAR(50) PRIMARY KEY,
    identity VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'BUYER',
    otp_id VARCHAR(50),
    attempt_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    expired_at TIMESTAMP
);

CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255),
    display_name VARCHAR(100),
    avatar_url TEXT,
    role VARCHAR(20) DEFAULT 'BUYER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    email_verified_at TIMESTAMP,
    phone_verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE kyc_profiles (
    kyc_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    doc_type VARCHAR(50),
    blob_url TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    admin_id VARCHAR(50),
    reason TEXT,
    submitted_at TIMESTAMP,
    approved_at TIMESTAMP,
    expired_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE backup_codes (
    code_hash VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE security_audits (
    audit_id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(100),
    description TEXT,
    ip_address VARCHAR(50),
    device_id VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE auth_sessions (
    session_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    ip_address VARCHAR(50),
    user_agent TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE social_accounts (
    provider_user_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    provider VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE user_addresses (
    address_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    full_address TEXT,
    phone VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE user_consents (
    consent_id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    consent_type VARCHAR(50),
    version VARCHAR(20),
    agreed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE agent_identities (
    agent_id VARCHAR(50) PRIMARY KEY,
    owner_id VARCHAR(50) NOT NULL,
    key_hash VARCHAR(255) NOT NULL,
    permissions JSON,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    expiry TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

CREATE TABLE user_preferences (
    user_id VARCHAR(50) PRIMARY KEY,
    language VARCHAR(10),
    currency VARCHAR(10),
    timezone VARCHAR(50),
    theme VARCHAR(20),
    notification_settings JSON,
    ai_privacy_settings JSON,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ==============================================================================
-- MODULE 2: UCP GATEWAY (Cổng giao tiếp AI)
-- ==============================================================================

CREATE TABLE agent_registries (
    agent_id VARCHAR(50) PRIMARY KEY, -- Khóa mềm tới Identity
    owner_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE agent_sessions (
    session_id VARCHAR(50) PRIMARY KEY,
    agent_id VARCHAR(50) NOT NULL,
    context_data JSON,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    FOREIGN KEY (agent_id) REFERENCES agent_registries(agent_id)
);

CREATE TABLE ucp_transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL,
    raw_input TEXT,
    intent_data JSON,
    proposal_data JSON,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES agent_sessions(session_id)
);

CREATE TABLE agent_quotas (
    agent_id VARCHAR(50) PRIMARY KEY,
    daily_limit DECIMAL(15,2),
    monthly_limit DECIMAL(15,2),
    current_usage DECIMAL(15,2) DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'VND',
    FOREIGN KEY (agent_id) REFERENCES agent_registries(agent_id)
);

CREATE TABLE ucp_configurations (
    version VARCHAR(20) PRIMARY KEY,
    mapping_rules JSON,
    release_notes TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    effective_at TIMESTAMP
);

-- ==============================================================================
-- MODULE 3: CATALOG (Quản lý Danh mục, Sản phẩm & Gian hàng)
-- ==============================================================================

CREATE TABLE merchants (
    merchant_id VARCHAR(50) PRIMARY KEY,
    owner_id VARCHAR(50) NOT NULL, -- Khóa mềm tới Identity
    name VARCHAR(150),
    logo_url TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE categories (
    category_id VARCHAR(50) PRIMARY KEY,
    parent_id VARCHAR(50),
    name VARCHAR(150),
    slug VARCHAR(150),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE brands (
    brand_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150),
    logo_url TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE attribute_templates (
    template_id VARCHAR(50) PRIMARY KEY,
    category_id VARCHAR(50),
    attributes JSON,
    is_filterable BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE products (
    product_id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    category_id VARCHAR(50),
    brand_id VARCHAR(50),
    name VARCHAR(255),
    image_list JSON,
    ai_description TEXT,
    tags JSON,
    collection_ids JSON,
    status VARCHAR(20) DEFAULT 'DRAFT',
    FOREIGN KEY (merchant_id) REFERENCES merchants(merchant_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (brand_id) REFERENCES brands(brand_id)
);

CREATE TABLE product_variants (
    sku_id VARCHAR(50) PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    attribute_values JSON,
    price DECIMAL(15,2),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- ==============================================================================
-- MODULE 4: INVENTORY (Quản lý Tồn kho & Giữ chỗ Saga)
-- ==============================================================================

CREATE TABLE warehouses (
    warehouse_id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL, -- Khóa mềm tới Catalog
    address TEXT,
    priority_level INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE inventory_items (
    sku_id VARCHAR(50) NOT NULL, -- Khóa mềm tới Catalog
    warehouse_id VARCHAR(50) NOT NULL,
    physical_qty INT DEFAULT 0,
    available_qty INT DEFAULT 0,
    safety_stock_qty INT DEFAULT 0,
    batch_no VARCHAR(50),
    expiry_date DATE,
    PRIMARY KEY (sku_id, warehouse_id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id)
);

CREATE TABLE stock_transfers (
    transfer_id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    from_warehouse_id VARCHAR(50) NOT NULL,
    to_warehouse_id VARCHAR(50) NOT NULL,
    sku_id VARCHAR(50) NOT NULL,
    qty INT NOT NULL,
    status VARCHAR(20) DEFAULT 'INITIATED'
);

CREATE TABLE stock_reservations (
    reservation_id VARCHAR(50) PRIMARY KEY,
    sku_id VARCHAR(50) NOT NULL,
    warehouse_id VARCHAR(50) NOT NULL,
    order_id VARCHAR(50), -- Khóa mềm tới Ordering
    qty INT NOT NULL,
    status VARCHAR(20) DEFAULT 'RESERVED',
    expires_at TIMESTAMP
);

CREATE TABLE stock_adjustments (
    adj_id VARCHAR(50) PRIMARY KEY,
    sku_id VARCHAR(50) NOT NULL,
    warehouse_id VARCHAR(50) NOT NULL,
    qty INT NOT NULL,
    type VARCHAR(20),
    reason TEXT
);

-- ==============================================================================
-- MODULE 5: ORDERING (Đặt hàng)
-- ==============================================================================

CREATE TABLE carts (
    cart_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL -- Khóa mềm tới Identity
);

CREATE TABLE cart_items (
    cart_id VARCHAR(50) NOT NULL,
    sku_id VARCHAR(50) NOT NULL, -- Khóa mềm tới Catalog
    qty INT NOT NULL,
    PRIMARY KEY (cart_id, sku_id),
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id)
);

CREATE TABLE orders (
    order_id VARCHAR(50) PRIMARY KEY,
    parent_order_id VARCHAR(50),
    user_id VARCHAR(50) NOT NULL,
    merchant_id VARCHAR(50) NOT NULL,
    proposal_id VARCHAR(50), -- Khóa mềm tới UCP
    total_amount DECIMAL(15,2),
    address_id VARCHAR(50) NOT NULL,
    payment_method_id VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_order_id) REFERENCES orders(order_id)
);

CREATE TABLE order_items (
    order_id VARCHAR(50) NOT NULL,
    sku_id VARCHAR(50) NOT NULL,
    qty INT NOT NULL,
    price DECIMAL(15,2),
    PRIMARY KEY (order_id, sku_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

CREATE TABLE order_returns (
    return_id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    merchant_id VARCHAR(50) NOT NULL,
    refund_amount DECIMAL(15,2),
    return_warehouse_id VARCHAR(50),
    reason TEXT,
    item_condition TEXT,
    status VARCHAR(20) DEFAULT 'REQUESTED',
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- ==============================================================================
-- MODULE 6: PAYMENT (Thanh toán & Đối soát)
-- ==============================================================================

CREATE TABLE payment_methods (
    method_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL, -- Khóa mềm tới Identity
    provider VARCHAR(50),
    last_4_digits VARCHAR(4),
    status VARCHAR(20) DEFAULT 'LINKED',
    verified_at TIMESTAMP
);

CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL, -- Khóa mềm tới Ordering
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

-- ==============================================================================
-- MODULE 7: SHIPPING (Giao vận)
-- ==============================================================================

CREATE TABLE shipments (
    shipment_id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL, -- Khóa mềm tới Ordering
    tracking_code VARCHAR(100),
    ghn_order_id VARCHAR(100),
    weight DECIMAL(10,2),
    dimensions VARCHAR(50),
    to_name VARCHAR(100),
    to_phone VARCHAR(20),
    to_address TEXT,
    to_ward_code VARCHAR(20),
    cod_amount DECIMAL(15,2),
    label_url TEXT,
    status VARCHAR(30) DEFAULT 'REQUESTED'
);

CREATE TABLE shipping_rates (
    rate_id VARCHAR(50) PRIMARY KEY,
    zone_code VARCHAR(50),
    base_fee DECIMAL(15,2),
    condition TEXT,
    configured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 8: NOTIFICATION (Thông báo đa kênh)
-- ==============================================================================

CREATE TABLE notification_templates (
    template_id VARCHAR(50) PRIMARY KEY,
    type VARCHAR(50),
    content TEXT,
    placeholders JSON,
    version INT DEFAULT 1
);

CREATE TABLE notifications (
    noti_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL, -- Khóa mềm tới Identity
    channel VARCHAR(20),
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'SENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE device_tokens (
    token_id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    device_token VARCHAR(255) NOT NULL,
    os VARCHAR(20),
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_subscriptions (
    user_id VARCHAR(50) PRIMARY KEY,
    settings_map JSON
);

CREATE TABLE notification_configurations (
    provider_id VARCHAR(50) PRIMARY KEY,
    provider_type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    configured_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_analytics (
    report_id VARCHAR(50) PRIMARY KEY,
    campaign_id VARCHAR(50),
    sent_count INT DEFAULT 0,
    read_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 9: PROMOTION (Khuyến mãi & Mã giảm giá)
-- ==============================================================================

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
    user_id VARCHAR(50) NOT NULL, -- Khóa mềm tới Identity
    order_id VARCHAR(50), -- Khóa mềm tới Ordering
    status VARCHAR(20) DEFAULT 'CLAIMED',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voucher_code) REFERENCES vouchers(voucher_code)
);

-- ==============================================================================
-- INDEXES FOR PERFORMANCE
-- ==============================================================================

-- Identity Module
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_security_audits_user ON security_audits(user_id, timestamp);

-- Catalog Module
CREATE INDEX idx_products_merchant ON products(merchant_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);

-- Inventory Module
CREATE INDEX idx_inventory_sku ON inventory_items(sku_id);
CREATE INDEX idx_stock_reservations_order ON stock_reservations(order_id);

-- Ordering Module
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_merchant ON orders(merchant_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at);

-- Payment Module
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);

-- Shipping Module
CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_tracking ON shipments(tracking_code);

-- Notification Module
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_created ON notifications(created_at);
