package com.ecommerce.identity.application.dto.auth;

import java.time.LocalDateTime;

public record LoginResult(
        String userId,
        String sessionId,
        String accessToken,
        LocalDateTime expiresAt) {
}
