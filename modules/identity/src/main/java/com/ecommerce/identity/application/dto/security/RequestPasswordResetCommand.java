package com.ecommerce.identity.application.dto.security;

public record RequestPasswordResetCommand(
        String identity,
        String clientIp
) {
}
