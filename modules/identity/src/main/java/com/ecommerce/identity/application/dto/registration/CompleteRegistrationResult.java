package com.ecommerce.identity.application.dto.registration;

import java.time.LocalDateTime;

public record CompleteRegistrationResult(
        String userId,
        String displayName,
        LocalDateTime createdAt) {
}
