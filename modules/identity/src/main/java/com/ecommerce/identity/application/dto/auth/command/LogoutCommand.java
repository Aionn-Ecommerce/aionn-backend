package com.ecommerce.identity.application.dto.auth.command;

public record LogoutCommand(
                String userId,
                String sessionId) {
}


