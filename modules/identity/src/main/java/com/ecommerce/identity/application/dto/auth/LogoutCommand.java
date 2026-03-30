package com.ecommerce.identity.application.dto.auth;

public record LogoutCommand(
        String userId,
        String sessionId
) {
}
