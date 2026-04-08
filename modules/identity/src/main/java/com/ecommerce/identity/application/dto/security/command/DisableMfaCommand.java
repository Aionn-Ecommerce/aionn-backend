package com.ecommerce.identity.application.dto.security.command;

public record DisableMfaCommand(
                String userId,
                String password,
                String clientIp) {
}


