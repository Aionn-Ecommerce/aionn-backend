package com.ecommerce.identity.application.dto.security;

public record RegenerateBackupCodesCommand(
        String userId,
        String clientIp
) {
}
