-- SHIPPING MODULE - COMPLETE INIT SCHEMA

CREATE TABLE shipments (
    shipment_id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
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

CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_tracking ON shipments(tracking_code);
