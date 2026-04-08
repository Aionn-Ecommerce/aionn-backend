package com.ecommerce.identity.application.dto.auth.view;

import java.time.LocalDateTime;

public record AuthSessionView(
                String sessionId,
                String userId,
                String status,
                String ipAddress,
                String userAgent,
                LocalDateTime createdAt,
                LocalDateTime lastActiveAt,
                LocalDateTime expiresAt) {
}
