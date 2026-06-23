CREATE TABLE notification_templates (
    template_id  VARCHAR(50) PRIMARY KEY,
    event_type   VARCHAR(100) NOT NULL,
    channel      VARCHAR(20)  NOT NULL,
    category     VARCHAR(20)  NOT NULL,
    locale       VARCHAR(20)  NOT NULL DEFAULT 'vi-VN',
    subject      TEXT,
    content      TEXT NOT NULL,
    placeholders JSONB NOT NULL DEFAULT '[]'::jsonb,
    version      INT  NOT NULL DEFAULT 1,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX idx_notification_templates_event_channel_locale
    ON notification_templates(event_type, channel, locale);

CREATE TABLE notifications (
    noti_id              VARCHAR(50) PRIMARY KEY,
    user_id              VARCHAR(50) NOT NULL,
    template_id          VARCHAR(50),
    channel              VARCHAR(20) NOT NULL,
    category             VARCHAR(20) NOT NULL,
    priority             VARCHAR(20) NOT NULL,
    subject              TEXT,
    content              TEXT NOT NULL,
    campaign_id          VARCHAR(50),
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count          INT NOT NULL DEFAULT 0,
    last_failure_reason  TEXT,
    version              BIGINT      NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at              TIMESTAMPTZ,
    read_at              TIMESTAMPTZ,
    deleted_at           TIMESTAMPTZ
);
CREATE INDEX idx_notifications_user_status     ON notifications(user_id, status);
CREATE INDEX idx_notifications_campaign_status ON notifications(campaign_id, status);
CREATE INDEX idx_notifications_status_retry    ON notifications(status, retry_count);

CREATE TABLE device_tokens (
    token_id      VARCHAR(50) PRIMARY KEY,
    user_id       VARCHAR(50) NOT NULL,
    device_token  VARCHAR(512) NOT NULL,
    os            VARCHAR(20),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    version       BIGINT      NOT NULL DEFAULT 0,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_device_tokens_user              ON device_tokens(user_id);
CREATE UNIQUE INDEX idx_device_tokens_user_token ON device_tokens(user_id, device_token);

CREATE TABLE notification_subscriptions (
    user_id    VARCHAR(50) PRIMARY KEY,
    settings   JSONB NOT NULL DEFAULT '{}'::jsonb,
    version    BIGINT      NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE notification_configurations (
    provider_id           VARCHAR(50) PRIMARY KEY,
    channel               VARCHAR(20) NOT NULL,
    provider_type         VARCHAR(50) NOT NULL,
    config                JSONB NOT NULL DEFAULT '{}'::jsonb,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    rate_limit_per_minute INT NOT NULL DEFAULT 60,
    configured_by         VARCHAR(50),
    version               BIGINT      NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_provider_channel_active ON notification_configurations(channel, is_active);

-- ---------------------------------------------------------------------
-- Default identity-driven templates (vi-VN). Event-type strings match
-- IdentityNotificationAdapter and IdentityEventListener.
-- ---------------------------------------------------------------------
INSERT INTO notification_templates (template_id, event_type, channel, category, locale, subject, content, placeholders, version, is_active)
VALUES
('idn_pwd_reset_em_vi',
 'identity.password-reset-requested', 'EMAIL', 'SECURITY', 'vi-VN',
 'Yeu cau dat lai mat khau',
 'Ban vua yeu cau dat lai mat khau. Token: {{resetToken}}. Neu khong phai ban, vui long bo qua email nay.',
 '["resetToken"]'::jsonb, 1, TRUE),

('idn_pwd_changed_em_vi',
 'identity.password-changed', 'EMAIL', 'SECURITY', 'vi-VN',
 'Mat khau da duoc thay doi',
 'Mat khau tai khoan Aionn cua ban vua duoc thay doi (kenh: {{channelHint}}). Neu khong phai ban, hay lien he ho tro ngay.',
 '["channelHint"]'::jsonb, 1, TRUE),

('idn_pwd_changed_in_vi',
 'identity.password-changed', 'IN_APP', 'SECURITY', 'vi-VN',
 'Mat khau da duoc thay doi',
 'Mat khau cua ban vua duoc thay doi qua {{channelHint}}.',
 '["channelHint"]'::jsonb, 1, TRUE),

('idn_email_changed_em_vi',
 'identity.email-changed', 'EMAIL', 'SECURITY', 'vi-VN',
 'Email da duoc cap nhat',
 'Email tai khoan Aionn vua doi tu {{oldEmail}} sang {{newEmail}}. Neu khong phai ban, hay lien he ho tro ngay.',
 '["oldEmail","newEmail"]'::jsonb, 1, TRUE),

('idn_email_changed_in_vi',
 'identity.email-changed', 'IN_APP', 'SECURITY', 'vi-VN',
 'Email da duoc cap nhat',
 'Email tai khoan vua doi sang {{newEmail}}.',
 '["newEmail"]'::jsonb, 1, TRUE),

('idn_phone_changed_sms_vi',
 'identity.phone-changed', 'SMS', 'SECURITY', 'vi-VN',
 NULL,
 'Aionn: so dien thoai tai khoan vua doi tu {{oldPhone}} sang {{newPhone}}.',
 '["oldPhone","newPhone"]'::jsonb, 1, TRUE),

('idn_phone_changed_in_vi',
 'identity.phone-changed', 'IN_APP', 'SECURITY', 'vi-VN',
 'So dien thoai da thay doi',
 'So dien thoai tai khoan vua doi sang {{newPhone}}.',
 '["newPhone"]'::jsonb, 1, TRUE),

('idn_email_otp_em_vi',
 'identity.email-otp', 'EMAIL', 'SECURITY', 'vi-VN',
 'Ma xac thuc Aionn',
 'Ma xac thuc email cua ban la {{otpCode}}. Hieu luc 5 phut. Vui long khong chia se voi nguoi khac.',
 '["otpCode"]'::jsonb, 1, TRUE),

('idn_phone_otp_sms_vi',
 'identity.phone-otp', 'SMS', 'SECURITY', 'vi-VN',
 NULL,
 'Aionn: ma xac thuc cua ban la {{otpCode}}. Hieu luc 5 phut. Khong chia se voi ai.',
 '["otpCode"]'::jsonb, 1, TRUE),

('idn_reg_otp_sms_vi',
 'identity.registration-otp', 'SMS', 'SECURITY', 'vi-VN',
 NULL,
 'Aionn: ma OTP dang ky cua ban la {{otpCode}}. Hieu luc 5 phut. Khong chia se voi ai.',
 '["otpCode"]'::jsonb, 1, TRUE);


-- ---------------------------------------------------------------------
-- Chat -> notification fan-out templates. Triggered by
-- MessageSentIntegrationEvent for offline recipients only.
-- ---------------------------------------------------------------------
INSERT INTO notification_templates (template_id, event_type, channel, category, locale, subject, content, placeholders, version, is_active)
VALUES
('chat_msg_in_app_vi',
 'chat.message-received', 'IN_APP', 'CHAT', 'vi-VN',
 '{{senderName}} vừa gửi tin nhắn',
 '{{messagePreview}}',
 '["senderName","messagePreview","conversationId"]'::jsonb, 1, TRUE),

('chat_msg_email_vi',
 'chat.message-received', 'EMAIL', 'CHAT', 'vi-VN',
 'Tin nhắn mới từ {{senderName}}',
 '{{senderName}} đã gửi cho bạn một tin nhắn:\n\n{{messagePreview}}\n\nMở Aionn để trả lời.',
 '["senderName","messagePreview","conversationId"]'::jsonb, 1, TRUE),

('chat_msg_push_vi',
 'chat.message-received', 'PUSH', 'CHAT', 'vi-VN',
 '{{senderName}}',
 '{{messagePreview}}',
 '["senderName","messagePreview","conversationId"]'::jsonb, 1, TRUE);
