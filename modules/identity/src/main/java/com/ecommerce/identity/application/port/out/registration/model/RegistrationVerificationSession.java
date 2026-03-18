package com.ecommerce.identity.application.port.out.registration.model;

import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationVerificationSession implements Serializable {

    private String regId;
    private String phoneNumber;
    private String otpCode;
    private int attemptCount;
    private int maxVerifyAttempts;
    private LocalDateTime resendAvailableAt;
    private LocalDateTime expiredAt;
    private boolean verified;
    private String verificationToken;
    private LocalDateTime verifiedAt;

    public boolean isExpired() {
        return expiredAt != null && expiredAt.isBefore(LocalDateTime.now());
    }

    public void verify(String inputOtpCode) {
        if (isExpired()) {
            throw new IdentityException(IdentityErrorCode.OTP_EXPIRED);
        }

        if (attemptCount >= maxVerifyAttempts) {
            throw new IdentityException(IdentityErrorCode.OTP_ATTEMPTS_EXCEEDED);
        }

        if (!Objects.equals(otpCode, inputOtpCode)) {
            attemptCount++;
            if (attemptCount >= maxVerifyAttempts) {
                throw new IdentityException(IdentityErrorCode.OTP_ATTEMPTS_EXCEEDED);
            }
            throw new IdentityException(IdentityErrorCode.OTP_INVALID);
        }

        verified = true;
        verificationToken = IdGenerator.ulid();
        verifiedAt = LocalDateTime.now();
    }
}