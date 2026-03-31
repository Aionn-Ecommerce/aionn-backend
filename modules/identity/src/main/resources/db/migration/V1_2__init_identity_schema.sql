CREATE TABLE users (
    user_id VARCHAR(26) PRIMARY KEY,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(255),
    display_name VARCHAR(100),
    avatar_url TEXT,
    role VARCHAR(20),
    status VARCHAR(20),
    email_verified_at TIMESTAMP,
    phone_verified_at TIMESTAMP,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
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
    blob_url TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    admin_id VARCHAR(26),
    reason TEXT,
    submitted_at TIMESTAMP NOT NULL,
    approved_at TIMESTAMP,
    expired_at TIMESTAMP,
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

-- ============================================================================
-- SELF-SERVICE TABLES
-- ============================================================================

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

-- ============================================================================
-- GEOGRAPHY TABLES (for address reference data)
-- ============================================================================

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

-- ============================================================================
-- ADDRESS TABLES
-- ============================================================================

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

-- ============================================================================
-- INDEXES
-- ============================================================================

-- Users indexes
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- User roles indexes
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- KYC indexes
CREATE INDEX idx_kyc_user_id ON kyc_profiles(user_id);
CREATE INDEX idx_kyc_status ON kyc_profiles(status);

-- Backup codes indexes
CREATE INDEX idx_backup_user_id ON backup_codes(user_id);

-- Security audits indexes
CREATE INDEX idx_audit_user_id ON security_audits(user_id);
CREATE INDEX idx_audit_event_type ON security_audits(event_type);

-- Auth sessions indexes
CREATE INDEX idx_session_user_id ON auth_sessions(user_id);
CREATE INDEX idx_session_status ON auth_sessions(status);
CREATE INDEX idx_session_expires_at ON auth_sessions(expires_at);

-- Social accounts indexes
CREATE INDEX idx_social_user_id ON social_accounts(user_id);

-- User consents indexes
CREATE INDEX idx_user_consents_user_id ON user_consents(user_id);
CREATE INDEX idx_user_consents_type ON user_consents(consent_type);

-- Agent identities indexes
CREATE INDEX idx_agent_owner_id ON agent_identities(owner_id);
CREATE INDEX idx_agent_status ON agent_identities(status);

-- Account deletion indexes
CREATE INDEX idx_account_deletion_user_id ON account_deletion_requests(user_id);
CREATE INDEX idx_account_deletion_status ON account_deletion_requests(status);

-- Data export indexes
CREATE INDEX idx_data_export_user_id ON data_export_requests(user_id);
CREATE INDEX idx_data_export_status ON data_export_requests(status);

-- Geography indexes
CREATE INDEX idx_province_country ON provinces(country_code);
CREATE INDEX idx_district_province ON districts(province_code);
CREATE INDEX idx_ward_district ON wards(district_code);

-- Address indexes
CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);
CREATE UNIQUE INDEX idx_user_addresses_one_default_per_user ON user_addresses (user_id) WHERE (is_default = TRUE);

-- ============================================================================
-- SEED DATA - Vietnam Geography
-- ============================================================================

-- Insert Vietnam as default country
INSERT INTO countries (code, name, name_en, phone_code, active) VALUES
('VN', 'Việt Nam', 'Vietnam', '+84', TRUE);

-- Insert major provinces in Vietnam
INSERT INTO provinces (code, name, name_en, country_code, active) VALUES
('VN-HN', 'Hà Nội', 'Hanoi', 'VN', TRUE),
('VN-SG', 'TP. Hồ Chí Minh', 'Ho Chi Minh City', 'VN', TRUE),
('VN-DN', 'Đà Nẵng', 'Da Nang', 'VN', TRUE),
('VN-HP', 'Hải Phòng', 'Hai Phong', 'VN', TRUE),
('VN-CT', 'Cần Thơ', 'Can Tho', 'VN', TRUE);

-- Insert districts for Hanoi
INSERT INTO districts (code, name, name_en, province_code, active) VALUES
('VN-HN-BA', 'Ba Đình', 'Ba Dinh', 'VN-HN', TRUE),
('VN-HN-HK', 'Hoàn Kiếm', 'Hoan Kiem', 'VN-HN', TRUE),
('VN-HN-TX', 'Tây Hồ', 'Tay Ho', 'VN-HN', TRUE),
('VN-HN-LB', 'Long Biên', 'Long Bien', 'VN-HN', TRUE),
('VN-HN-CG', 'Cầu Giấy', 'Cau Giay', 'VN-HN', TRUE),
('VN-HN-DD', 'Đống Đa', 'Dong Da', 'VN-HN', TRUE),
('VN-HN-HBT', 'Hai Bà Trưng', 'Hai Ba Trung', 'VN-HN', TRUE),
('VN-HN-HM', 'Hoàng Mai', 'Hoang Mai', 'VN-HN', TRUE),
('VN-HN-TL', 'Thanh Xuân', 'Thanh Xuan', 'VN-HN', TRUE);

-- Insert districts for Ho Chi Minh City
INSERT INTO districts (code, name, name_en, province_code, active) VALUES
('VN-SG-Q1', 'Quận 1', 'District 1', 'VN-SG', TRUE),
('VN-SG-Q2', 'Quận 2', 'District 2', 'VN-SG', TRUE),
('VN-SG-Q3', 'Quận 3', 'District 3', 'VN-SG', TRUE),
('VN-SG-Q4', 'Quận 4', 'District 4', 'VN-SG', TRUE),
('VN-SG-Q5', 'Quận 5', 'District 5', 'VN-SG', TRUE),
('VN-SG-Q6', 'Quận 6', 'District 6', 'VN-SG', TRUE),
('VN-SG-Q7', 'Quận 7', 'District 7', 'VN-SG', TRUE),
('VN-SG-Q8', 'Quận 8', 'District 8', 'VN-SG', TRUE),
('VN-SG-Q9', 'Quận 9', 'District 9', 'VN-SG', TRUE),
('VN-SG-Q10', 'Quận 10', 'District 10', 'VN-SG', TRUE),
('VN-SG-Q11', 'Quận 11', 'District 11', 'VN-SG', TRUE),
('VN-SG-Q12', 'Quận 12', 'District 12', 'VN-SG', TRUE),
('VN-SG-TD', 'Thủ Đức', 'Thu Duc', 'VN-SG', TRUE),
('VN-SG-BT', 'Bình Thạnh', 'Binh Thanh', 'VN-SG', TRUE),
('VN-SG-PN', 'Phú Nhuận', 'Phu Nhuan', 'VN-SG', TRUE),
('VN-SG-TB', 'Tân Bình', 'Tan Binh', 'VN-SG', TRUE),
('VN-SG-TP', 'Tân Phú', 'Tan Phu', 'VN-SG', TRUE),
('VN-SG-GV', 'Gò Vấp', 'Go Vap', 'VN-SG', TRUE),
('VN-SG-BTA', 'Bình Tân', 'Binh Tan', 'VN-SG', TRUE);

-- Insert wards for Ba Dinh district (Hanoi)
INSERT INTO wards (code, name, name_en, district_code, active) VALUES
('VN-HN-BA-PX', 'Phúc Xá', 'Phuc Xa', 'VN-HN-BA', TRUE),
('VN-HN-BA-TT', 'Trúc Bạch', 'Truc Bach', 'VN-HN-BA', TRUE),
('VN-HN-BA-VT', 'Vĩnh Phúc', 'Vinh Phuc', 'VN-HN-BA', TRUE),
('VN-HN-BA-CK', 'Cống Vị', 'Cong Vi', 'VN-HN-BA', TRUE),
('VN-HN-BA-LT', 'Liễu Giai', 'Lieu Giai', 'VN-HN-BA', TRUE),
('VN-HN-BA-NB', 'Nguyễn Trung Trực', 'Nguyen Trung Truc', 'VN-HN-BA', TRUE),
('VN-HN-BA-QT', 'Quán Thánh', 'Quan Thanh', 'VN-HN-BA', TRUE),
('VN-HN-BA-NP', 'Ngọc Hà', 'Ngoc Ha', 'VN-HN-BA', TRUE),
('VN-HN-BA-DK', 'Điện Biên', 'Dien Bien', 'VN-HN-BA', TRUE),
('VN-HN-BA-DT', 'Đội Cấn', 'Doi Can', 'VN-HN-BA', TRUE),
('VN-HN-BA-NK', 'Ngọc Khánh', 'Ngoc Khanh', 'VN-HN-BA', TRUE),
('VN-HN-BA-KM', 'Kim Mã', 'Kim Ma', 'VN-HN-BA', TRUE),
('VN-HN-BA-GV', 'Giảng Võ', 'Giang Vo', 'VN-HN-BA', TRUE),
('VN-HN-BA-TH', 'Thành Công', 'Thanh Cong', 'VN-HN-BA', TRUE);

-- Insert wards for District 1 (Ho Chi Minh City)
INSERT INTO wards (code, name, name_en, district_code, active) VALUES
('VN-SG-Q1-BN', 'Bến Nghé', 'Ben Nghe', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-BT', 'Bến Thành', 'Ben Thanh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-NT', 'Nguyễn Thái Bình', 'Nguyen Thai Binh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-PNL', 'Phạm Ngũ Lão', 'Pham Ngu Lao', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-CK', 'Cầu Kho', 'Cau Kho', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-OC', 'Cầu Ông Lãnh', 'Cau Ong Lanh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-DK', 'Đa Kao', 'Da Kao', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-TT', 'Tân Định', 'Tan Dinh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-NK', 'Nguyễn Cư Trinh', 'Nguyen Cu Trinh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-CL', 'Cô Giang', 'Co Giang', 'VN-SG-Q1', TRUE);
