package com.ecommerce.identity.application.dto.security.command;

public record RegenerateBackupCodesCommand(
                String userId,
                String clientIp) {
}


