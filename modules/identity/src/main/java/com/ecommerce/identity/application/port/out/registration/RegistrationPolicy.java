package com.ecommerce.identity.application.port.out.registration;

public interface RegistrationPolicy {

    int getMaxVerifyAttempts();

    int getResendCooldownSeconds();

    int getOtpExpirySeconds();

    long getSessionExpiresDays();

    String getDefaultCountryCallingCode();

    boolean isExposeOtpInResponse();

    int getIpRateLimitMaxAttempts();

    int getIpRateLimitWindowSeconds();

    int getPhoneRateLimitMaxAttempts();

    int getPhoneRateLimitWindowSeconds();
}
