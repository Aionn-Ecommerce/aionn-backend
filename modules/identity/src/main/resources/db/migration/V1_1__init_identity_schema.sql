CREATE TABLE users (
    user_id VARCHAR(26) PRIMARY KEY,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(255),
    display_name VARCHAR(100),
    avatar_url TEXT,
    status VARCHAR(20),
    email_verified_at TIMESTAMP,
    phone_verified_at TIMESTAMP,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(64),
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE user_roles (
    user_id VARCHAR(26) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE kyc_profiles (
    kyc_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    doc_type VARCHAR(50) NOT NULL,
    blob_url TEXT,
    status VARCHAR(20) NOT NULL,
    reviewer_id VARCHAR(26),
    review_note TEXT,
    decision_admin_id VARCHAR(26),
    reject_reason TEXT,
    submitted_at TIMESTAMP,
    approved_at TIMESTAMP,
    expired_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    provider VARCHAR(30),
    provider_applicant_id VARCHAR(64),
    provider_level_name VARCHAR(128),
    provider_review_status VARCHAR(50),
    provider_correlation_id VARCHAR(128),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE backup_codes (
    backup_code_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    code_hash VARCHAR(255) NOT NULL UNIQUE,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE security_audits (
    audit_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    event_type VARCHAR(100),
    description TEXT,
    ip_address VARCHAR(50),
    device_id VARCHAR(100),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE auth_sessions (
    session_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    ip_address VARCHAR(50),
    user_agent TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE social_accounts (
    social_account_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_social_provider_id UNIQUE (provider, provider_user_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE user_consents (
    consent_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    consent_type VARCHAR(50) NOT NULL,
    version VARCHAR(20) NOT NULL,
    agreed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    ip_address VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE agent_identities (
    agent_id VARCHAR(26) PRIMARY KEY,
    owner_id VARCHAR(26) NOT NULL,
    key_hash VARCHAR(255) NOT NULL,
    permissions JSONB,
    status VARCHAR(20),
    expiry_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

CREATE TABLE user_preferences (
    user_id VARCHAR(26) PRIMARY KEY,
    language VARCHAR(10),
    currency VARCHAR(10),
    timezone VARCHAR(50),
    theme VARCHAR(20),
    notification_settings JSONB,
    ai_privacy_settings JSONB,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE account_deletion_requests (
    deletion_request_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    scheduled_deletion_at TIMESTAMP NOT NULL,
    canceled_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE data_export_requests (
    export_request_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    file_url TEXT,
    completed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE countries (
    code VARCHAR(2) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    phone_code VARCHAR(10),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_country_name UNIQUE (name)
);

CREATE TABLE provinces (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    country_code VARCHAR(2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_province_country FOREIGN KEY (country_code) REFERENCES countries(code)
);

CREATE TABLE districts (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    province_code VARCHAR(10) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_district_province FOREIGN KEY (province_code) REFERENCES provinces(code)
);

CREATE TABLE wards (
    code VARCHAR(15) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    district_code VARCHAR(10) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_ward_district FOREIGN KEY (district_code) REFERENCES districts(code)
);

CREATE TABLE user_addresses (
    address_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    province_code VARCHAR(20) NOT NULL,
    province_name VARCHAR(255),
    district_code VARCHAR(20) NOT NULL,
    district_name VARCHAR(255),
    ward_code VARCHAR(20) NOT NULL,
    ward_name VARCHAR(255),
    detail_address VARCHAR(500) NOT NULL,
    full_address TEXT NOT NULL,
    address_type VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_username ON users(username);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

CREATE INDEX idx_kyc_user_id ON kyc_profiles(user_id);
CREATE INDEX idx_kyc_status ON kyc_profiles(status);
CREATE UNIQUE INDEX idx_kyc_provider_applicant_id
    ON kyc_profiles (provider_applicant_id)
    WHERE provider_applicant_id IS NOT NULL;

CREATE INDEX idx_backup_user_id ON backup_codes(user_id);

CREATE INDEX idx_audit_user_id ON security_audits(user_id);
CREATE INDEX idx_audit_event_type ON security_audits(event_type);

CREATE INDEX idx_session_user_id ON auth_sessions(user_id);
CREATE INDEX idx_session_status ON auth_sessions(status);
CREATE INDEX idx_session_expires_at ON auth_sessions(expires_at);
CREATE INDEX idx_session_user_status ON auth_sessions(user_id, status);

CREATE INDEX idx_social_user_id ON social_accounts(user_id);

CREATE INDEX idx_user_consents_user_id ON user_consents(user_id);
CREATE INDEX idx_user_consents_type ON user_consents(consent_type);

CREATE INDEX idx_agent_owner_id ON agent_identities(owner_id);
CREATE INDEX idx_agent_status ON agent_identities(status);

CREATE INDEX idx_account_deletion_user_id ON account_deletion_requests(user_id);
CREATE INDEX idx_account_deletion_status ON account_deletion_requests(status);
CREATE UNIQUE INDEX idx_account_deletion_one_pending_per_user
    ON account_deletion_requests (user_id)
    WHERE status = 'PENDING';

CREATE INDEX idx_data_export_user_id ON data_export_requests(user_id);
CREATE INDEX idx_data_export_status ON data_export_requests(status);
CREATE UNIQUE INDEX idx_data_export_one_active_per_user
    ON data_export_requests (user_id)
    WHERE status IN ('REQUESTED', 'PROCESSING');

CREATE INDEX idx_province_country ON provinces(country_code);
CREATE INDEX idx_district_province ON districts(province_code);
CREATE INDEX idx_ward_district ON wards(district_code);

CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);
CREATE UNIQUE INDEX idx_user_addresses_one_default_per_user
    ON user_addresses (user_id)
    WHERE is_default = TRUE;

INSERT INTO countries (code, name, name_en, phone_code, active) VALUES
('VN', 'Viet Nam', 'Vietnam', '+84', TRUE);

INSERT INTO provinces (code, name, name_en, country_code, active) VALUES
('VN-HN', 'Ha Noi', 'Hanoi', 'VN', TRUE),
('VN-SG', 'Ho Chi Minh City', 'Ho Chi Minh City', 'VN', TRUE),
('VN-DN', 'Da Nang', 'Da Nang', 'VN', TRUE),
('VN-HP', 'Hai Phong', 'Hai Phong', 'VN', TRUE),
('VN-CT', 'Can Tho', 'Can Tho', 'VN', TRUE);

INSERT INTO districts (code, name, name_en, province_code, active) VALUES
('VN-HN-BA', 'Ba Dinh', 'Ba Dinh', 'VN-HN', TRUE),
('VN-HN-HK', 'Hoan Kiem', 'Hoan Kiem', 'VN-HN', TRUE),
('VN-HN-TX', 'Tay Ho', 'Tay Ho', 'VN-HN', TRUE),
('VN-HN-LB', 'Long Bien', 'Long Bien', 'VN-HN', TRUE),
('VN-HN-CG', 'Cau Giay', 'Cau Giay', 'VN-HN', TRUE),
('VN-HN-DD', 'Dong Da', 'Dong Da', 'VN-HN', TRUE),
('VN-HN-HBT', 'Hai Ba Trung', 'Hai Ba Trung', 'VN-HN', TRUE),
('VN-HN-HM', 'Hoang Mai', 'Hoang Mai', 'VN-HN', TRUE),
('VN-HN-TL', 'Thanh Xuan', 'Thanh Xuan', 'VN-HN', TRUE),
('VN-SG-Q1', 'District 1', 'District 1', 'VN-SG', TRUE),
('VN-SG-Q2', 'District 2', 'District 2', 'VN-SG', TRUE),
('VN-SG-Q3', 'District 3', 'District 3', 'VN-SG', TRUE),
('VN-SG-Q4', 'District 4', 'District 4', 'VN-SG', TRUE),
('VN-SG-Q5', 'District 5', 'District 5', 'VN-SG', TRUE),
('VN-SG-Q6', 'District 6', 'District 6', 'VN-SG', TRUE),
('VN-SG-Q7', 'District 7', 'District 7', 'VN-SG', TRUE),
('VN-SG-Q8', 'District 8', 'District 8', 'VN-SG', TRUE),
('VN-SG-Q9', 'District 9', 'District 9', 'VN-SG', TRUE),
('VN-SG-Q10', 'District 10', 'District 10', 'VN-SG', TRUE),
('VN-SG-Q11', 'District 11', 'District 11', 'VN-SG', TRUE),
('VN-SG-Q12', 'District 12', 'District 12', 'VN-SG', TRUE),
('VN-SG-TD', 'Thu Duc', 'Thu Duc', 'VN-SG', TRUE),
('VN-SG-BT', 'Binh Thanh', 'Binh Thanh', 'VN-SG', TRUE),
('VN-SG-PN', 'Phu Nhuan', 'Phu Nhuan', 'VN-SG', TRUE),
('VN-SG-TB', 'Tan Binh', 'Tan Binh', 'VN-SG', TRUE),
('VN-SG-TP', 'Tan Phu', 'Tan Phu', 'VN-SG', TRUE),
('VN-SG-GV', 'Go Vap', 'Go Vap', 'VN-SG', TRUE),
('VN-SG-BTA', 'Binh Tan', 'Binh Tan', 'VN-SG', TRUE),
('VN-DN-HC', 'Hai Chau', 'Hai Chau', 'VN-DN', TRUE),
('VN-DN-TK', 'Thanh Khe', 'Thanh Khe', 'VN-DN', TRUE),
('VN-DN-ST', 'Son Tra', 'Son Tra', 'VN-DN', TRUE),
('VN-HP-HB', 'Hong Bang', 'Hong Bang', 'VN-HP', TRUE),
('VN-HP-LC', 'Le Chan', 'Le Chan', 'VN-HP', TRUE),
('VN-CT-NK', 'Ninh Kieu', 'Ninh Kieu', 'VN-CT', TRUE),
('VN-CT-BT', 'Binh Thuy', 'Binh Thuy', 'VN-CT', TRUE);

INSERT INTO wards (code, name, name_en, district_code, active) VALUES
('VN-HN-BA-PX', 'Phuc Xa', 'Phuc Xa', 'VN-HN-BA', TRUE),
('VN-HN-BA-TT', 'Truc Bach', 'Truc Bach', 'VN-HN-BA', TRUE),
('VN-HN-BA-VT', 'Vinh Phuc', 'Vinh Phuc', 'VN-HN-BA', TRUE),
('VN-HN-BA-CK', 'Cong Vi', 'Cong Vi', 'VN-HN-BA', TRUE),
('VN-HN-BA-LT', 'Lieu Giai', 'Lieu Giai', 'VN-HN-BA', TRUE),
('VN-HN-BA-NB', 'Nguyen Trung Truc', 'Nguyen Trung Truc', 'VN-HN-BA', TRUE),
('VN-HN-BA-QT', 'Quan Thanh', 'Quan Thanh', 'VN-HN-BA', TRUE),
('VN-HN-BA-NP', 'Ngoc Ha', 'Ngoc Ha', 'VN-HN-BA', TRUE),
('VN-HN-BA-DK', 'Dien Bien', 'Dien Bien', 'VN-HN-BA', TRUE),
('VN-HN-BA-DT', 'Doi Can', 'Doi Can', 'VN-HN-BA', TRUE),
('VN-HN-BA-NK', 'Ngoc Khanh', 'Ngoc Khanh', 'VN-HN-BA', TRUE),
('VN-HN-BA-KM', 'Kim Ma', 'Kim Ma', 'VN-HN-BA', TRUE),
('VN-HN-BA-GV', 'Giang Vo', 'Giang Vo', 'VN-HN-BA', TRUE),
('VN-HN-BA-TH', 'Thanh Cong', 'Thanh Cong', 'VN-HN-BA', TRUE),
('VN-SG-Q1-BN', 'Ben Nghe', 'Ben Nghe', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-BT', 'Ben Thanh', 'Ben Thanh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-NT', 'Nguyen Thai Binh', 'Nguyen Thai Binh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-PNL', 'Pham Ngu Lao', 'Pham Ngu Lao', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-CK', 'Cau Kho', 'Cau Kho', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-OC', 'Cau Ong Lanh', 'Cau Ong Lanh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-DK', 'Da Kao', 'Da Kao', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-TT', 'Tan Dinh', 'Tan Dinh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-NK', 'Nguyen Cu Trinh', 'Nguyen Cu Trinh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-CL', 'Co Giang', 'Co Giang', 'VN-SG-Q1', TRUE),
('VN-DN-HC-W1', 'Ward 1', 'Ward 1', 'VN-DN-HC', TRUE),
('VN-DN-TK-W1', 'Ward 1', 'Ward 1', 'VN-DN-TK', TRUE),
('VN-DN-ST-W1', 'Ward 1', 'Ward 1', 'VN-DN-ST', TRUE),
('VN-HP-HB-W1', 'Ward 1', 'Ward 1', 'VN-HP-HB', TRUE),
('VN-HP-LC-W1', 'Ward 1', 'Ward 1', 'VN-HP-LC', TRUE),
('VN-CT-NK-W1', 'Ward 1', 'Ward 1', 'VN-CT-NK', TRUE),
('VN-CT-BT-W1', 'Ward 1', 'Ward 1', 'VN-CT-BT', TRUE),
('VN-HN-HK-W1', 'Hang Bac', 'Hang Bac', 'VN-HN-HK', TRUE),
('VN-HN-TX-W1', 'Buoi', 'Buoi', 'VN-HN-TX', TRUE),
('VN-HN-LB-W1', 'Duc Giang', 'Duc Giang', 'VN-HN-LB', TRUE),
('VN-HN-CG-W1', 'Dich Vong', 'Dich Vong', 'VN-HN-CG', TRUE),
('VN-HN-DD-W1', 'Khuong Thuong', 'Khuong Thuong', 'VN-HN-DD', TRUE),
('VN-HN-HBT-W1', 'Bach Dang', 'Bach Dang', 'VN-HN-HBT', TRUE),
('VN-HN-HM-W1', 'Dinh Cong', 'Dinh Cong', 'VN-HN-HM', TRUE),
('VN-HN-TL-W1', 'Phuong Liet', 'Phuong Liet', 'VN-HN-TL', TRUE),
('VN-SG-Q2-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q2', TRUE),
('VN-SG-Q3-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q3', TRUE),
('VN-SG-Q4-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q4', TRUE),
('VN-SG-Q5-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q5', TRUE),
('VN-SG-Q6-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q6', TRUE),
('VN-SG-Q7-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q7', TRUE),
('VN-SG-Q8-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q8', TRUE),
('VN-SG-Q9-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q9', TRUE),
('VN-SG-Q10-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q10', TRUE),
('VN-SG-Q11-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q11', TRUE),
('VN-SG-Q12-W1', 'Ward 1', 'Ward 1', 'VN-SG-Q12', TRUE),
('VN-SG-TD-W1', 'Ward 1', 'Ward 1', 'VN-SG-TD', TRUE),
('VN-SG-BT-W1', 'Ward 1', 'Ward 1', 'VN-SG-BT', TRUE),
('VN-SG-PN-W1', 'Ward 1', 'Ward 1', 'VN-SG-PN', TRUE),
('VN-SG-TB-W1', 'Ward 1', 'Ward 1', 'VN-SG-TB', TRUE),
('VN-SG-TP-W1', 'Ward 1', 'Ward 1', 'VN-SG-TP', TRUE),
('VN-SG-GV-W1', 'Ward 1', 'Ward 1', 'VN-SG-GV', TRUE),
('VN-SG-BTA-W1', 'Ward 1', 'Ward 1', 'VN-SG-BTA', TRUE);
