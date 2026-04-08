package com.ecommerce.identity.application.dto.agent.query;

public record GetAgentAuditLogsQuery(
        String ownerUserId,
        String agentId) {
}


