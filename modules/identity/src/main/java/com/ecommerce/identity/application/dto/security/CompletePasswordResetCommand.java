package com.ecommerce.identity.application.dto.security;

public record CompletePasswordResetCommand(
        String token,
        String newPassword,
        String clientIp
) {
}
