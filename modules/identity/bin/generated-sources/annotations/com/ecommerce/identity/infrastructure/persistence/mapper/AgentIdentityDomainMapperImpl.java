package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.AgentIdentity;
import com.ecommerce.identity.domain.valueobject.AgentStatus;
import com.ecommerce.identity.infrastructure.persistence.entity.AgentIdentityEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:30:12+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AgentIdentityDomainMapperImpl implements AgentIdentityDomainMapper {

    @Override
    public AgentIdentityEntity toEntity(AgentIdentity domain) {
        if ( domain == null ) {
            return null;
        }

        AgentIdentityEntity.AgentIdentityEntityBuilder agentIdentityEntity = AgentIdentityEntity.builder();

        agentIdentityEntity.agentId( domain.getId() );
        agentIdentityEntity.expiryAt( domain.getExpiresAt() );
        agentIdentityEntity.keyHash( domain.getKeyHash() );
        agentIdentityEntity.permissions( domain.getPermissions() );
        agentIdentityEntity.createdAt( domain.getCreatedAt() );

        agentIdentityEntity.status( domain.getStatus().name() );

        return agentIdentityEntity.build();
    }

    @Override
    public AgentIdentity toDomain(AgentIdentityEntity entity) {
        if ( entity == null ) {
            return null;
        }

        AgentIdentity.AgentIdentityBuilder agentIdentity = AgentIdentity.builder();

        agentIdentity.id( entity.getAgentId() );
        agentIdentity.ownerId( entityOwnerUserId( entity ) );
        agentIdentity.name( entity.getAgentId() );
        agentIdentity.expiresAt( entity.getExpiryAt() );
        agentIdentity.keyHash( entity.getKeyHash() );
        agentIdentity.permissions( entity.getPermissions() );
        agentIdentity.createdAt( entity.getCreatedAt() );

        agentIdentity.status( AgentStatus.valueOf(entity.getStatus()) );

        return agentIdentity.build();
    }

    private String entityOwnerUserId(AgentIdentityEntity agentIdentityEntity) {
        UserEntity owner = agentIdentityEntity.getOwner();
        if ( owner == null ) {
            return null;
        }
        return owner.getUserId();
    }
}
