-- =====================================================================
-- SHIPPING MODULE - INIT SCHEMA
-- =====================================================================

CREATE TABLE shipments (
    shipment_id            VARCHAR(50) PRIMARY KEY,
    order_id               VARCHAR(50) NOT NULL,
    tracking_code          VARCHAR(100),
    carrier_order_id       VARCHAR(100),
    label_url              TEXT,
    weight_gram            INT NOT NULL DEFAULT 0,
    length_cm              NUMERIC(8,2),
    width_cm               NUMERIC(8,2),
    height_cm              NUMERIC(8,2),
    to_full_name           VARCHAR(255),
    to_phone               VARCHAR(30),
    to_address_line        TEXT,
    to_ward_code           VARCHAR(30),
    to_district_id         VARCHAR(30),
    to_province_code       VARCHAR(30),
    to_country_code        VARCHAR(5),
    cod_amount             NUMERIC(18,2),
    shipping_fee           NUMERIC(18,2),
    currency               VARCHAR(3),
    current_location       VARCHAR(255),
    shipper_name           VARCHAR(255),
    shipper_phone          VARCHAR(30),
    signature_url          TEXT,
    attempt_count          INT NOT NULL DEFAULT 0,
    last_failure_reason    TEXT,
    issue_type             VARCHAR(50),
    issue_resolution       TEXT,
    expected_delivery_date TIMESTAMPTZ,
    status                 VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    picked_at              TIMESTAMPTZ,
    delivered_at           TIMESTAMPTZ,
    cancelled_at           TIMESTAMPTZ,
    returned_at            TIMESTAMPTZ
);
CREATE INDEX idx_shipments_order    ON shipments(order_id);
CREATE UNIQUE INDEX idx_shipments_tracking ON shipments(tracking_code) WHERE tracking_code IS NOT NULL;
CREATE INDEX idx_shipments_status   ON shipments(status);

CREATE TABLE shipping_rates (
    rate_id    VARCHAR(50) PRIMARY KEY,
    zone_code  VARCHAR(50) NOT NULL UNIQUE,
    base_fee   NUMERIC(18,2) NOT NULL,
    currency   VARCHAR(3)  NOT NULL,
    condition  TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
