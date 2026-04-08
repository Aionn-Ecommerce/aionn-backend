package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.SecurityAudit;
import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:49+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class SecurityAuditDomainMapperImpl implements SecurityAuditDomainMapper {

    @Override
    public SecurityAuditEntity toEntity(SecurityAudit domain) {
        if ( domain == null ) {
            return null;
        }

        SecurityAuditEntity.SecurityAuditEntityBuilder securityAuditEntity = SecurityAuditEntity.builder();

        securityAuditEntity.auditId( domain.getId() );
        securityAuditEntity.eventType( domain.getEventType() );
        securityAuditEntity.description( domain.getDescription() );
        securityAuditEntity.ipAddress( domain.getIpAddress() );
        securityAuditEntity.deviceId( domain.getDeviceId() );
        securityAuditEntity.timestamp( domain.getTimestamp() );

        return securityAuditEntity.build();
    }

    @Override
    public SecurityAudit toDomain(SecurityAuditEntity entity) {
        if ( entity == null ) {
            return null;
        }

        SecurityAudit.SecurityAuditBuilder securityAudit = SecurityAudit.builder();

        securityAudit.id( entity.getAuditId() );
        securityAudit.userId( entityUserUserId( entity ) );
        securityAudit.eventType( entity.getEventType() );
        securityAudit.description( entity.getDescription() );
        securityAudit.ipAddress( entity.getIpAddress() );
        securityAudit.deviceId( entity.getDeviceId() );
        securityAudit.timestamp( entity.getTimestamp() );

        return securityAudit.build();
    }

    private String entityUserUserId(SecurityAuditEntity securityAuditEntity) {
        UserEntity user = securityAuditEntity.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUserId();
    }
}
