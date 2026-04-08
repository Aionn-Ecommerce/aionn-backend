package com.ecommerce.identity.application.dto.security.command;

public record CompletePasswordResetCommand(
                String token,
                String newPassword,
                String clientIp) {
}


