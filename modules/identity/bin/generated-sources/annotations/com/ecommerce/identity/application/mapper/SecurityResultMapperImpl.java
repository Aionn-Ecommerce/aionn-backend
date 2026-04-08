package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.security.result.SecurityAuditLogResult;
import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class SecurityResultMapperImpl implements SecurityResultMapper {

    @Override
    public SecurityAuditLogResult toAuditLogResult(SecurityAuditEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String auditId = null;
        String eventType = null;
        String description = null;
        String ipAddress = null;
        String deviceId = null;
        LocalDateTime timestamp = null;

        auditId = entity.getAuditId();
        eventType = entity.getEventType();
        description = entity.getDescription();
        ipAddress = entity.getIpAddress();
        deviceId = entity.getDeviceId();
        timestamp = entity.getTimestamp();

        SecurityAuditLogResult securityAuditLogResult = new SecurityAuditLogResult( auditId, eventType, description, ipAddress, deviceId, timestamp );

        return securityAuditLogResult;
    }
}
