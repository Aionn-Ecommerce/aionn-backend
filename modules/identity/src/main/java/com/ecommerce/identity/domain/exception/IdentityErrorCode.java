package com.ecommerce.identity.domain.exception;

public enum IdentityErrorCode {
    PHONE_ALREADY_EXISTS("IDENTITY_001", "Phone number already exists in the system"),
    EMAIL_ALREADY_EXISTS("IDENTITY_002", "Email already exists in the system"),
    REGISTRATION_NOT_FOUND("IDENTITY_003", "Registration session not found"),
    REGISTRATION_EXPIRED("IDENTITY_004", "Registration session has expired"),

    OTP_INVALID("IDENTITY_101", "Invalid OTP code"),
    OTP_EXPIRED("IDENTITY_102", "OTP code has expired"),
    OTP_ATTEMPTS_EXCEEDED("IDENTITY_103", "OTP attempts exceeded the allowed limit"),
    CAPTCHA_INVALID("IDENTITY_104", "Invalid captcha token"),
    VERIFICATION_TOKEN_INVALID("IDENTITY_105", "Verification Token invalid"),
    RATE_LIMIT_EXCEEDED("IDENTITY_106", "Rate limit exceeded"),

    USER_NOT_FOUND("IDENTITY_201", "User not found"),
    INVALID_DISPLAY_NAME("IDENTITY_202", "Invalid display name");

    private final String code;
    private final String defaultMessage;

    IdentityErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}