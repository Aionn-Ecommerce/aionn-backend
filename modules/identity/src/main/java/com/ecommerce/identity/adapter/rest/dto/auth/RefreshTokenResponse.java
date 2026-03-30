package com.ecommerce.identity.adapter.rest.dto.auth;

import java.time.LocalDateTime;

public record RefreshTokenResponse(
        String userId,
        String sessionId,
        String accessToken,
        LocalDateTime expiresAt) {
}
