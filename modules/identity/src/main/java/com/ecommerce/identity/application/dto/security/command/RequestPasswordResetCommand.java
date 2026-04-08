package com.ecommerce.identity.application.dto.security.command;

public record RequestPasswordResetCommand(
                String identity,
                String clientIp) {
}


