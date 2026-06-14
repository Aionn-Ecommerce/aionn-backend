-- Seed Shipping Rates

INSERT INTO shipping_rates (rate_id, zone_code, base_fee, currency, condition, version, created_at, updated_at) VALUES
('SR_VN_HN', 'VN-HN', 30000.0, 'VND', 'Hanoi Delivery', 0, NOW(), NOW()),
('SR_VN_SG', 'VN-SG', 35000.0, 'VND', 'HCMC Delivery', 0, NOW(), NOW()),
('SR_VN_DN', 'VN-DN', 40000.0, 'VND', 'Da Nang Delivery', 0, NOW(), NOW());
