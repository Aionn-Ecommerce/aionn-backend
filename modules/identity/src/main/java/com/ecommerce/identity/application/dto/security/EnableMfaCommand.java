package com.ecommerce.identity.application.dto.security;

public record EnableMfaCommand(
        String userId,
        String password,
        String clientIp
) {
}
