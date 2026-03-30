package com.ecommerce.identity.application.dto.agent;

public record RevokeAgentCommand(
        String ownerUserId,
        String agentId
) {
}
