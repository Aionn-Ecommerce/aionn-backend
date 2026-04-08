package com.ecommerce.identity.application.dto.consent.command;

public record AgreePrivacyCommand(
        String userId,
        String version,
        String clientIp) {
}
