package com.ecommerce.identity.application.dto.auth;

import java.time.LocalDateTime;

public record AuthSessionView(
        String sessionId,
        String status,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt,
        LocalDateTime lastActiveAt,
        LocalDateTime expiresAt) {
}
