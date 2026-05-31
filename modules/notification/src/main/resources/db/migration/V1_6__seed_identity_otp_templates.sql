INSERT INTO notification_templates (
    template_id, event_type, channel, category, locale, subject, content, placeholders, version, is_active
) VALUES
    (
        'id_email_otp_vi',
        'IDENTITY_EMAIL_OTP',
        'EMAIL',
        'SECURITY',
        'vi-VN',
        'Ma xac thuc tai khoan',
        'Ma OTP cua ban la {{otpCode}}. Ma nay se het han sau vai phut.',
        '["otpCode"]'::jsonb,
        1,
        TRUE
    ),
    (
        'id_phone_otp_vi',
        'IDENTITY_PHONE_OTP',
        'SMS',
        'SECURITY',
        'vi-VN',
        NULL,
        'Ma OTP cua ban la {{otpCode}}.',
        '["otpCode"]'::jsonb,
        1,
        TRUE
    ),
    (
        'id_reg_otp_vi',
        'IDENTITY_REGISTRATION_OTP',
        'SMS',
        'SECURITY',
        'vi-VN',
        NULL,
        'Ma xac minh dang ky cua ban la {{otpCode}}.',
        '["otpCode"]'::jsonb,
        1,
        TRUE
    );
