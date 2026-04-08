package com.ecommerce.identity.application.dto.auth.command;

public record LoginCommand(
                String identity,
                String password,
                String ipAddress,
                String userAgent) {
}
