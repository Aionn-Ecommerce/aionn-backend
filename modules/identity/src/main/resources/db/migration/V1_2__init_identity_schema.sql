-- IDENTITY MODULE - COMPLETE INIT SCHEMA

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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
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

CREATE TABLE user_addresses (
    address_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    full_address TEXT NOT NULL,
    phone VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
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

CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

CREATE INDEX idx_kyc_user_id ON kyc_profiles(user_id);
CREATE INDEX idx_kyc_status ON kyc_profiles(status);

CREATE INDEX idx_backup_user_id ON backup_codes(user_id);

CREATE INDEX idx_audit_user_id ON security_audits(user_id);
CREATE INDEX idx_audit_event_type ON security_audits(event_type);

CREATE INDEX idx_session_user_id ON auth_sessions(user_id);
CREATE INDEX idx_session_status ON auth_sessions(status);
CREATE INDEX idx_session_expires_at ON auth_sessions(expires_at);

CREATE INDEX idx_social_user_id ON social_accounts(user_id);

CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);

CREATE INDEX idx_user_consents_user_id ON user_consents(user_id);
CREATE INDEX idx_user_consents_type ON user_consents(consent_type);

CREATE INDEX idx_agent_owner_id ON agent_identities(owner_id);
CREATE INDEX idx_agent_status ON agent_identities(status);
