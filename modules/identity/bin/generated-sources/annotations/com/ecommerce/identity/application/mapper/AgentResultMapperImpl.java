package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.agent.result.AgentAuditLogResult;
import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import com.ecommerce.identity.domain.model.AgentIdentity;
import com.ecommerce.identity.domain.model.SecurityAudit;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:35:27+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AgentResultMapperImpl implements AgentResultMapper {

    @Override
    public AgentIdentityResult toIdentityResult(AgentIdentity agentIdentity) {
        if ( agentIdentity == null ) {
            return null;
        }

        String agentId = null;
        String status = null;
        LocalDateTime expiryAt = null;
        String keyHash = null;
        String permissions = null;
        LocalDateTime createdAt = null;

        agentId = agentIdentity.getId();
        status = statusToString( agentIdentity.getStatus() );
        expiryAt = agentIdentity.getExpiresAt();
        keyHash = agentIdentity.getKeyHash();
        permissions = agentIdentity.getPermissions();
        createdAt = agentIdentity.getCreatedAt();

        AgentIdentityResult agentIdentityResult = new AgentIdentityResult( agentId, keyHash, permissions, status, expiryAt, createdAt );

        return agentIdentityResult;
    }

    @Override
    public AgentAuditLogResult toAuditLogResult(SecurityAudit securityAudit) {
        if ( securityAudit == null ) {
            return null;
        }

        String auditId = null;
        String eventType = null;
        String description = null;
        String ipAddress = null;
        String deviceId = null;
        LocalDateTime timestamp = null;

        auditId = securityAudit.getId();
        eventType = securityAudit.getEventType();
        description = securityAudit.getDescription();
        ipAddress = securityAudit.getIpAddress();
        deviceId = securityAudit.getDeviceId();
        timestamp = securityAudit.getTimestamp();

        AgentAuditLogResult agentAuditLogResult = new AgentAuditLogResult( auditId, eventType, description, ipAddress, deviceId, timestamp );

        return agentAuditLogResult;
    }
}
