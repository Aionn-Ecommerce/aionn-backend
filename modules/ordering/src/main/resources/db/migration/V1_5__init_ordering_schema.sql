-- ORDERING MODULE - COMPLETE INIT SCHEMA

CREATE TABLE carts (
    cart_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL
);

CREATE TABLE cart_items (
    cart_id VARCHAR(50) NOT NULL,
    sku_id VARCHAR(50) NOT NULL,
    qty INT NOT NULL,
    PRIMARY KEY (cart_id, sku_id),
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id)
);

CREATE TABLE orders (
    order_id VARCHAR(50) PRIMARY KEY,
    parent_order_id VARCHAR(50),
    user_id VARCHAR(50) NOT NULL,
    merchant_id VARCHAR(50) NOT NULL,
    proposal_id VARCHAR(50),
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

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_merchant ON orders(merchant_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at);
