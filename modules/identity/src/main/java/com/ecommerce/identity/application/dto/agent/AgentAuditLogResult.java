package com.ecommerce.identity.application.dto.agent;

import java.time.LocalDateTime;

public record AgentAuditLogResult(
        String auditId,
        String eventType,
        String description,
        String ipAddress,
        String deviceId,
        LocalDateTime timestamp
) {
}
