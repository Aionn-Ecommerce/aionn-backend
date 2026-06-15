-- =====================================================================
-- PROMOTION MODULE - CREATE PROMOTION BANNERS
-- =====================================================================

CREATE TABLE promotion_banners (
    banner_id      VARCHAR(50) PRIMARY KEY,
    title          VARCHAR(150) NOT NULL,
    image_url      VARCHAR(500) NOT NULL,
    link_url       VARCHAR(500) NOT NULL,
    display_order  INT NOT NULL DEFAULT 0,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    version        BIGINT NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_promotion_banners_active ON promotion_banners(active, display_order);

-- Seed Initial Banners
INSERT INTO promotion_banners (banner_id, title, image_url, link_url, display_order, active, version, created_at, updated_at) VALUES
('BAN_001', 'Aionn Siêu Sale Công Nghệ', '/images/banners/tech_sale.png', '/categories/CAT_ELE', 1, TRUE, 0, NOW(), NOW()),
('BAN_002', 'Aionn Siêu Sale Thời Trang', '/images/banners/fashion_sale.png', '/categories/CAT_FAS', 2, TRUE, 0, NOW(), NOW()),
('BAN_003', 'Aionn Trang Trí Nhà Cửa', '/images/banners/home_decor.png', '/categories/CAT_HOK', 3, TRUE, 0, NOW(), NOW());
