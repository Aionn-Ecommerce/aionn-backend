package com.ecommerce.identity.application.dto.consent;

public record UpdateMarketingConsentCommand(
        String userId,
        boolean subscribed,
        String clientIp
) {
}
