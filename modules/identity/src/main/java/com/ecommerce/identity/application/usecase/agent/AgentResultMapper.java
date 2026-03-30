package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.AgentAuditLogResult;
import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;
import com.ecommerce.identity.infrastructure.persistence.entity.AgentIdentityEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;

final class AgentResultMapper {

    private AgentResultMapper() {
    }

    static AgentIdentityResult toIdentityResult(AgentIdentityEntity entity) {
        return new AgentIdentityResult(
                entity.getAgentId(),
                entity.getKeyHash(),
                entity.getPermissions(),
                entity.getStatus(),
                entity.getExpiryAt(),
                entity.getCreatedAt());
    }

    static AgentAuditLogResult toAuditLogResult(SecurityAuditEntity entity) {
        return new AgentAuditLogResult(
                entity.getAuditId(),
                entity.getEventType(),
                entity.getDescription(),
                entity.getIpAddress(),
                entity.getDeviceId(),
                entity.getTimestamp());
    }
}
