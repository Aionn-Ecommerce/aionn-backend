package com.ecommerce.identity.application.dto.security;

public record ChangePasswordCommand(
        String userId,
        String currentPassword,
        String newPassword,
        String clientIp
) {
}
