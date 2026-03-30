-- NOTIFICATION MODULE - COMPLETE INIT SCHEMA

CREATE TABLE notification_templates (
    template_id VARCHAR(50) PRIMARY KEY,
    type VARCHAR(50),
    content TEXT,
    placeholders JSON,
    version INT DEFAULT 1
);

CREATE TABLE notifications (
    noti_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    channel VARCHAR(20),
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'SENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE device_tokens (
    token_id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    device_token VARCHAR(255) NOT NULL,
    os VARCHAR(20),
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_subscriptions (
    user_id VARCHAR(50) PRIMARY KEY,
    settings_map JSON
);

CREATE TABLE notification_configurations (
    provider_id VARCHAR(50) PRIMARY KEY,
    provider_type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    configured_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_analytics (
    report_id VARCHAR(50) PRIMARY KEY,
    campaign_id VARCHAR(50),
    sent_count INT DEFAULT 0,
    read_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_created ON notifications(created_at);
