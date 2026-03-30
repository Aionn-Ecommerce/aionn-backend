package com.ecommerce.identity.application.dto.security;

public record DisableMfaCommand(
        String userId,
        String password,
        String clientIp
) {
}
