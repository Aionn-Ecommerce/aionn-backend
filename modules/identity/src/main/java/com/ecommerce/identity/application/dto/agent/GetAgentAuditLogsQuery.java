package com.ecommerce.identity.application.dto.agent;

public record GetAgentAuditLogsQuery(
                String ownerUserId,
                String agentId) {
}
