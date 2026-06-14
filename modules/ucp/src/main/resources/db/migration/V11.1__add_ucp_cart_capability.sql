-- Add UCP cart capability and cart_id linkage in checkout
-- Per spec: https://ucp.dev/specification/cart

-- Cart sessions table for lightweight basket building before checkout
CREATE TABLE IF NOT EXISTS ucp_cart_sessions (
    cart_id             VARCHAR(50)     PRIMARY KEY,
    user_id             VARCHAR(50),
    currency            VARCHAR(3)      NOT NULL,
    line_items_json     TEXT            NOT NULL,
    totals_json         TEXT,
    continue_url        VARCHAR(500),
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cart_user
    ON ucp_cart_sessions (user_id);

-- Add cart_id to checkout sessions for cart-to-checkout conversion
ALTER TABLE ucp_checkout_session 
    ADD COLUMN IF NOT EXISTS cart_id VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_ucp_checkout_session_cart
    ON ucp_checkout_session (cart_id);

COMMENT ON TABLE ucp_cart_sessions IS 'UCP cart sessions - lightweight basket building before checkout per UCP spec';
COMMENT ON COLUMN ucp_checkout_session.cart_id IS 'Source cart ID for cart-to-checkout conversion (idempotent)';
