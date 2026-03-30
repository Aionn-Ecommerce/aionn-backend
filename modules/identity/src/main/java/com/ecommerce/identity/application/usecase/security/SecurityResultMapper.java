package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.SecurityAuditLogResult;
import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;

final class SecurityResultMapper {

    private SecurityResultMapper() {
    }

    static SecurityAuditLogResult toAuditLogResult(SecurityAuditEntity entity) {
        return new SecurityAuditLogResult(
                entity.getAuditId(),
                entity.getEventType(),
                entity.getDescription(),
                entity.getIpAddress(),
                entity.getDeviceId(),
                entity.getTimestamp());
    }
}
