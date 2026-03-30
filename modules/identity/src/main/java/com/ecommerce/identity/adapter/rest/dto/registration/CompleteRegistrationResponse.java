package com.ecommerce.identity.adapter.rest.dto.registration;

import java.time.LocalDateTime;

public record CompleteRegistrationResponse(
        String userId,
        String displayName,
        LocalDateTime createdAt
) {
}
