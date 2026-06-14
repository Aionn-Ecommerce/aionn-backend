-- =====================================================================
-- Seed translation data for English (en-US) templates
-- =====================================================================

INSERT INTO notification_templates (template_id, event_type, channel, category, locale, subject, content, placeholders, version, is_active)
VALUES
('idn_pwd_reset_em_en',
 'identity.password-reset-requested', 'EMAIL', 'SECURITY', 'en-US',
 'Password Reset Request',
 'You have requested a password reset. Token: {{resetToken}}. If this was not you, please ignore this email.',
 '["resetToken"]'::jsonb, 1, TRUE),

('idn_pwd_changed_em_en',
 'identity.password-changed', 'EMAIL', 'SECURITY', 'en-US',
 'Your password has been changed',
 'Your Aionn account password has been changed (channel: {{channelHint}}). If this was not you, please contact support immediately.',
 '["channelHint"]'::jsonb, 1, TRUE),

('idn_pwd_changed_in_en',
 'identity.password-changed', 'IN_APP', 'SECURITY', 'en-US',
 'Password Changed',
 'Your password has been changed via {{channelHint}}.',
 '["channelHint"]'::jsonb, 1, TRUE),

('idn_email_changed_em_en',
 'identity.email-changed', 'EMAIL', 'SECURITY', 'en-US',
 'Email Address Updated',
 'Your Aionn account email address was changed from {{oldEmail}} to {{newEmail}}. If this was not you, please contact support immediately.',
 '["oldEmail","newEmail"]'::jsonb, 1, TRUE),

('idn_email_changed_in_en',
 'identity.email-changed', 'IN_APP', 'SECURITY', 'en-US',
 'Email Address Updated',
 'Your account email address has been updated to {{newEmail}}.',
 '["newEmail"]'::jsonb, 1, TRUE),

('idn_phone_changed_sms_en',
 'identity.phone-changed', 'SMS', 'SECURITY', 'en-US',
 NULL,
 'Aionn: your account phone number has been changed from {{oldPhone}} to {{newPhone}}.',
 '["oldPhone","newPhone"]'::jsonb, 1, TRUE),

('idn_phone_changed_in_en',
 'identity.phone-changed', 'IN_APP', 'SECURITY', 'en-US',
 'Phone Number Updated',
 'Your account phone number has been updated to {{newPhone}}.',
 '["newPhone"]'::jsonb, 1, TRUE),

('idn_email_otp_em_en',
 'identity.email-otp', 'EMAIL', 'SECURITY', 'en-US',
 'Aionn Verification Code',
 'Your email verification code is {{otpCode}}. Valid for 5 minutes. Please do not share it with others.',
 '["otpCode"]'::jsonb, 1, TRUE),

('idn_phone_otp_sms_en',
 'identity.phone-otp', 'SMS', 'SECURITY', 'en-US',
 NULL,
 'Aionn: your verification code is {{otpCode}}. Valid for 5 minutes. Do not share it.',
 '["otpCode"]'::jsonb, 1, TRUE),

('idn_reg_otp_sms_en',
 'identity.registration-otp', 'SMS', 'SECURITY', 'en-US',
 NULL,
 'Aionn: your registration OTP is {{otpCode}}. Valid for 5 minutes. Do not share it.',
 '["otpCode"]'::jsonb, 1, TRUE),

('chat_msg_in_app_en',
 'chat.message-received', 'IN_APP', 'CHAT', 'en-US',
 '{{senderName}} sent a message',
 '{{messagePreview}}',
 '["senderName","messagePreview","conversationId"]'::jsonb, 1, TRUE),

('chat_msg_email_en',
 'chat.message-received', 'EMAIL', 'CHAT', 'en-US',
 'New message from {{senderName}}',
 '{{senderName}} has sent you a message:\n\n{{messagePreview}}\n\nOpen Aionn to reply.',
 '["senderName","messagePreview","conversationId"]'::jsonb, 1, TRUE),

('chat_msg_push_en',
 'chat.message-received', 'PUSH', 'CHAT', 'en-US',
 '{{senderName}}',
 '{{messagePreview}}',
 '["senderName","messagePreview","conversationId"]'::jsonb, 1, TRUE);
