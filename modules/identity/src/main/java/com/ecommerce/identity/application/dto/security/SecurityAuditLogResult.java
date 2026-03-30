package com.ecommerce.identity.application.dto.security;

import java.time.LocalDateTime;

public record SecurityAuditLogResult(
        String auditId,
        String eventType,
        String description,
        String ipAddress,
        String deviceId,
        LocalDateTime timestamp
) {
}
