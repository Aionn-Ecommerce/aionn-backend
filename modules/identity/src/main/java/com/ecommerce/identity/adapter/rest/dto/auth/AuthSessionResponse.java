package com.ecommerce.identity.adapter.rest.dto.auth;

import java.time.LocalDateTime;

public record AuthSessionResponse(
        String sessionId,
        String status,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt,
        LocalDateTime lastActiveAt,
        LocalDateTime expiresAt) {
}
