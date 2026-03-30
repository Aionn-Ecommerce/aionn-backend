package com.ecommerce.identity.application.dto.agent;

public record UpdateAgentPermissionsCommand(
        String ownerUserId,
        String agentId,
        String permissionsJson
) {
}
