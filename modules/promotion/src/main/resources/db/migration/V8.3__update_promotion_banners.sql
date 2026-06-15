-- Update existing banner links to point to search page with category filter instead of non-existent category routes
UPDATE promotion_banners SET link_url = '/products?categoryId=CAT_ELE' WHERE banner_id = 'BAN_001';
UPDATE promotion_banners SET link_url = '/products?categoryId=CAT_FAS' WHERE banner_id = 'BAN_002';
UPDATE promotion_banners SET link_url = '/products?categoryId=CAT_HOK' WHERE banner_id = 'BAN_003';

-- Seed the 4th banner (Beauty / Health & Beauty)
INSERT INTO promotion_banners (banner_id, title, image_url, link_url, display_order, active, version, created_at, updated_at)
VALUES ('BAN_004', 'Aionn Siêu Sale Mỹ Phẩm', '/images/banners/beauty_sale.png', '/products?categoryId=CAT_HBE', 4, TRUE, 0, NOW(), NOW())
ON CONFLICT (banner_id) DO NOTHING;
