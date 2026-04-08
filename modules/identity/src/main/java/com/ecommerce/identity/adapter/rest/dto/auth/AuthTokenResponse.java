package com.ecommerce.identity.adapter.rest.dto.auth;

import java.time.LocalDateTime;

public record AuthTokenResponse(
                String userId,
                String sessionId,
                String refreshToken,
                String accessToken,
                LocalDateTime expiresAt) {
}
