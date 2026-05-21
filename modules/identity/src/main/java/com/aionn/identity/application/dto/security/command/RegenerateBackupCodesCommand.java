package com.aionn.identity.application.dto.security.command;

public record RegenerateBackupCodesCommand(
        String userId,
        String password,
        String clientIp) {
}

