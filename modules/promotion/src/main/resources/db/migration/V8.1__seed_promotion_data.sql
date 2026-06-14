-- Seed Campaigns and Vouchers

INSERT INTO promotion_campaigns (campaign_id, name, type, budget, budget_remaining, currency, start_date, end_date, created_by, status, min_order_value, max_claims_per_user, max_uses_per_voucher, version, created_at, updated_at) VALUES ('CAMP_MEGA', 'Summer Mega Sale', 'DISCOUNT', 500000000.00, 500000000.00, 'VND', '2026-06-13 16:42:22', '2026-09-11 16:42:22', 'sys_admin_01', 'ACTIVE', 200000.00, 5, 1000, 0, NOW(), NOW());

INSERT INTO vouchers (voucher_code, campaign_id, discount_amount, currency, usage_limit, used_count, reserved_count, valid_from, valid_until, version, created_at, updated_at) VALUES
('SUMMER50', 'CAMP_MEGA', 500000.0, 'VND', 500, 0, 0, '2026-06-13 16:42:22', '2026-09-11 16:42:22', 0, NOW(), NOW()),
('SUMMER10', 'CAMP_MEGA', 100000.0, 'VND', 1000, 0, 0, '2026-06-13 16:42:22', '2026-09-11 16:42:22', 0, NOW(), NOW()),
('FREESHIP', 'CAMP_MEGA', 30000.0, 'VND', 1000, 0, 0, '2026-06-13 16:42:22', '2026-09-11 16:42:22', 0, NOW(), NOW());
