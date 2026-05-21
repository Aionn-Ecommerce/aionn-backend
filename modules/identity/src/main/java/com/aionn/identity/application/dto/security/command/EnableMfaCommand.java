package com.aionn.identity.application.dto.security.command;

public record EnableMfaCommand(
                String userId,
                String password,
                String clientIp) {
}

