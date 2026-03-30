package com.ecommerce.identity.application.dto.auth;

public record RevokeSessionCommand(
        String userId,
        String sessionId
) {
}
