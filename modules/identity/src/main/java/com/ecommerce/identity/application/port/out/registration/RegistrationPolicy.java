package com.ecommerce.identity.application.port.out.registration;

public record RegistrationPolicy(
        int maxVerifyAttempts,
        int resendCooldownSeconds,
        int otpExpirySeconds,
        boolean exposeOtpInResponse) {

    public RegistrationPolicy {
        if (maxVerifyAttempts <= 0) {
            throw new IllegalArgumentException("maxVerifyAttempts must be greater than zero");
        }
        if (resendCooldownSeconds <= 0) {
            throw new IllegalArgumentException("resendCooldownSeconds must be greater than zero");
        }
        if (otpExpirySeconds <= 0) {
            throw new IllegalArgumentException("otpExpirySeconds must be greater than zero");
        }
    }
}
