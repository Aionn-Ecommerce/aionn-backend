package com.ecommerce.identity.application.dto.auth.result;

import java.time.LocalDateTime;

public record SocialLoginResult(
                String userId,
                String sessionId,
                String accessToken,
                LocalDateTime expiresAt,
                boolean newUser) {
}


