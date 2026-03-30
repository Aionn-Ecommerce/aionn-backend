package com.ecommerce.identity.adapter.rest.dto.registration;

import java.time.LocalDateTime;

public record InitiateRegistrationResponse(
                String regId,
                LocalDateTime resendAvailableAt,
                LocalDateTime expiredAt,
                String otpCode) {
}
