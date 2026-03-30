package com.ecommerce.identity.application.port.out.user.model;

import com.ecommerce.identity.application.port.out.user.OtpChannel;
import com.ecommerce.identity.application.port.out.user.UserOtpPurpose;

import java.time.LocalDateTime;

public record UserOtpChallenge(
        String userId,
        UserOtpPurpose purpose,
        OtpChannel channel,
        String target,
        String otpCode,
        String pendingValue,
        LocalDateTime expiresAt,
        int attempts) {

    public UserOtpChallenge withAttempts(int nextAttempts) {
        return new UserOtpChallenge(
                userId,
                purpose,
                channel,
                target,
                otpCode,
                pendingValue,
                expiresAt,
                nextAttempts);
    }
}
