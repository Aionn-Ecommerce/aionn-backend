package com.ecommerce.identity.application.dto.agent;

public record SuspendAgentCommand(
        String ownerUserId,
        String agentId
) {
}
