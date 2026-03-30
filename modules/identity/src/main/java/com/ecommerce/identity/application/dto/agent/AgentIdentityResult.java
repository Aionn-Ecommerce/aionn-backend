package com.ecommerce.identity.application.dto.agent;

import java.time.LocalDateTime;

public record AgentIdentityResult(
        String agentId,
        String keyHash,
        String permissions,
        String status,
        LocalDateTime expiryAt,
        LocalDateTime createdAt
) {
}
