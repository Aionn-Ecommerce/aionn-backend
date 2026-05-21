-- ============================================================================
-- Identity schema fixes (issues.md follow-up)
-- ============================================================================

-- 1. KYC: rename "reason" -> "review_note", add reviewer/decision/reject_reason,
--    relax submitted_at NOT NULL (a freshly created DRAFT has no submission yet)
--    and rename admin_id -> reviewer_id for clarity.
ALTER TABLE kyc_profiles
    RENAME COLUMN admin_id TO reviewer_id;

ALTER TABLE kyc_profiles
    RENAME COLUMN reason TO review_note;

ALTER TABLE kyc_profiles
    ADD COLUMN decision_admin_id VARCHAR(26),
    ADD COLUMN reject_reason TEXT,
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE kyc_profiles
    ALTER COLUMN submitted_at DROP NOT NULL,
    ALTER COLUMN blob_url DROP NOT NULL;

-- 2. Single-pending unique constraints prevent TOCTOU duplicates.
CREATE UNIQUE INDEX idx_account_deletion_one_pending_per_user
    ON account_deletion_requests (user_id) WHERE status = 'PENDING';

CREATE UNIQUE INDEX idx_data_export_one_active_per_user
    ON data_export_requests (user_id) WHERE status IN ('REQUESTED', 'PROCESSING');

-- 3. Failed login tracking column (used by AuthService.recordFailedLogin).
ALTER TABLE users
    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;

-- 4. Useful indexes that were missing.
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_session_user_status ON auth_sessions(user_id, status);

-- 5. Geography: backfill more provinces / districts so address creation works
--    outside Hanoi-Ba Dinh & HCM-District 1 during smoke tests.
INSERT INTO districts (code, name, name_en, province_code, active) VALUES
    ('VN-DN-HC', 'Hải Châu', 'Hai Chau', 'VN-DN', TRUE),
    ('VN-DN-TK', 'Thanh Khê', 'Thanh Khe', 'VN-DN', TRUE),
    ('VN-DN-ST', 'Sơn Trà', 'Son Tra', 'VN-DN', TRUE),
    ('VN-HP-HB', 'Hồng Bàng', 'Hong Bang', 'VN-HP', TRUE),
    ('VN-HP-LC', 'Lê Chân', 'Le Chan', 'VN-HP', TRUE),
    ('VN-CT-NK', 'Ninh Kiều', 'Ninh Kieu', 'VN-CT', TRUE),
    ('VN-CT-BT', 'Bình Thuỷ', 'Binh Thuy', 'VN-CT', TRUE);

-- One default ward per district to make addresses creatable everywhere.
INSERT INTO wards (code, name, name_en, district_code, active) VALUES
    ('VN-DN-HC-W1', 'Phường 1', 'Ward 1', 'VN-DN-HC', TRUE),
    ('VN-DN-TK-W1', 'Phường 1', 'Ward 1', 'VN-DN-TK', TRUE),
    ('VN-DN-ST-W1', 'Phường 1', 'Ward 1', 'VN-DN-ST', TRUE),
    ('VN-HP-HB-W1', 'Phường 1', 'Ward 1', 'VN-HP-HB', TRUE),
    ('VN-HP-LC-W1', 'Phường 1', 'Ward 1', 'VN-HP-LC', TRUE),
    ('VN-CT-NK-W1', 'Phường 1', 'Ward 1', 'VN-CT-NK', TRUE),
    ('VN-CT-BT-W1', 'Phường 1', 'Ward 1', 'VN-CT-BT', TRUE);

-- Default wards for HN districts that previously had none.
INSERT INTO wards (code, name, name_en, district_code, active) VALUES
    ('VN-HN-HK-W1', 'Hàng Bạc', 'Hang Bac', 'VN-HN-HK', TRUE),
    ('VN-HN-TX-W1', 'Bưởi', 'Buoi', 'VN-HN-TX', TRUE),
    ('VN-HN-LB-W1', 'Đức Giang', 'Duc Giang', 'VN-HN-LB', TRUE),
    ('VN-HN-CG-W1', 'Dịch Vọng', 'Dich Vong', 'VN-HN-CG', TRUE),
    ('VN-HN-DD-W1', 'Khương Thượng', 'Khuong Thuong', 'VN-HN-DD', TRUE),
    ('VN-HN-HBT-W1', 'Bạch Đằng', 'Bach Dang', 'VN-HN-HBT', TRUE),
    ('VN-HN-HM-W1', 'Định Công', 'Dinh Cong', 'VN-HN-HM', TRUE),
    ('VN-HN-TL-W1', 'Phương Liệt', 'Phuong Liet', 'VN-HN-TL', TRUE);

-- Default wards for HCM districts that previously had none.
INSERT INTO wards (code, name, name_en, district_code, active) VALUES
    ('VN-SG-Q2-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q2', TRUE),
    ('VN-SG-Q3-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q3', TRUE),
    ('VN-SG-Q4-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q4', TRUE),
    ('VN-SG-Q5-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q5', TRUE),
    ('VN-SG-Q6-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q6', TRUE),
    ('VN-SG-Q7-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q7', TRUE),
    ('VN-SG-Q8-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q8', TRUE),
    ('VN-SG-Q9-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q9', TRUE),
    ('VN-SG-Q10-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q10', TRUE),
    ('VN-SG-Q11-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q11', TRUE),
    ('VN-SG-Q12-W1', 'Phường 1', 'Ward 1', 'VN-SG-Q12', TRUE),
    ('VN-SG-TD-W1', 'Phường 1', 'Ward 1', 'VN-SG-TD', TRUE),
    ('VN-SG-BT-W1', 'Phường 1', 'Ward 1', 'VN-SG-BT', TRUE),
    ('VN-SG-PN-W1', 'Phường 1', 'Ward 1', 'VN-SG-PN', TRUE),
    ('VN-SG-TB-W1', 'Phường 1', 'Ward 1', 'VN-SG-TB', TRUE),
    ('VN-SG-TP-W1', 'Phường 1', 'Ward 1', 'VN-SG-TP', TRUE),
    ('VN-SG-GV-W1', 'Phường 1', 'Ward 1', 'VN-SG-GV', TRUE),
    ('VN-SG-BTA-W1', 'Phường 1', 'Ward 1', 'VN-SG-BTA', TRUE);
