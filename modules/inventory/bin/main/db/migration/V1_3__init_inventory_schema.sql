-- INVENTORY MODULE - COMPLETE INIT SCHEMA

CREATE TABLE warehouses (
    warehouse_id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    address TEXT,
    priority_level INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE inventory_items (
    sku_id VARCHAR(50) NOT NULL,
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
    order_id VARCHAR(50),
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

CREATE INDEX idx_inventory_sku ON inventory_items(sku_id);
CREATE INDEX idx_stock_reservations_order ON stock_reservations(order_id);
