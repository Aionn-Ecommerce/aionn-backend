package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.sharedkernel.util.IdGenerator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class RegistrationVerificationSession implements Serializable {

    private final String regId;
    private final String phoneNumber;
    private String otpCode;
    private final int maxVerifyAttempts;
    private LocalDateTime resendAvailableAt;
    private LocalDateTime expiredAt;
    private int attemptCount;
    private boolean verified;
    private String verificationToken;
    private LocalDateTime verifiedAt;

    public RegistrationVerificationSession(
            String regId,
            String phoneNumber,
            String otpCode,
            int attemptCount,
            int maxVerifyAttempts,
            LocalDateTime resendAvailableAt,
            LocalDateTime expiredAt,
            boolean verified,
            String verificationToken,
            LocalDateTime verifiedAt) {
        this.regId = regId;
        this.phoneNumber = phoneNumber;
        this.otpCode = otpCode;
        this.attemptCount = attemptCount;
        this.maxVerifyAttempts = maxVerifyAttempts;
        this.resendAvailableAt = resendAvailableAt;
        this.expiredAt = expiredAt;
        this.verified = verified;
        this.verificationToken = verificationToken;
        this.verifiedAt = verifiedAt;
    }

    public boolean isExpired() {
        return expiredAt != null && expiredAt.isBefore(LocalDateTime.now());
    }

    public boolean isLocked() {
        return attemptCount >= maxVerifyAttempts;
    }

    public void verify(String inputOtpCode) {
        if (isExpired()) {
            throw new IdentityException(IdentityErrorCode.OTP_EXPIRED);
        }
        if (isLocked()) {
            throw new IdentityException(IdentityErrorCode.OTP_ATTEMPTS_EXCEEDED);
        }
        if (!Objects.equals(otpCode, inputOtpCode)) {
            attemptCount++;
            if (isLocked()) {
                // Lock this session immediately once max attempts are reached.
                expiredAt = LocalDateTime.now();
                throw new IdentityException(IdentityErrorCode.OTP_ATTEMPTS_EXCEEDED);
            }
            throw new IdentityException(IdentityErrorCode.OTP_INVALID);
        }

        verified = true;
        verificationToken = IdGenerator.ulid();
        verifiedAt = LocalDateTime.now();
    }

    public void resend(String newOtpCode, LocalDateTime newResendAvailableAt, LocalDateTime newExpiredAt) {
        if (verified) {
            throw new IdentityException(IdentityErrorCode.REGISTRATION_ALREADY_VERIFIED);
        }
        if (isExpired()) {
            throw new IdentityException(IdentityErrorCode.REGISTRATION_SESSION_EXPIRED);
        }
        if (isLocked()) {
            throw new IdentityException(IdentityErrorCode.OTP_ATTEMPTS_EXCEEDED);
        }
        if (resendAvailableAt != null && LocalDateTime.now().isBefore(resendAvailableAt)) {
            throw new IdentityException(IdentityErrorCode.OTP_RESEND_TOO_SOON);
        }

        otpCode = newOtpCode;
        resendAvailableAt = newResendAvailableAt;
        expiredAt = newExpiredAt;
    }

    public String getRegId() {
        return regId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public int getMaxVerifyAttempts() {
        return maxVerifyAttempts;
    }

    public LocalDateTime getResendAvailableAt() {
        return resendAvailableAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
}


