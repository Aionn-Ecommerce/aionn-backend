package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.AuthSession;
import com.ecommerce.identity.domain.valueobject.AuthSessionStatus;
import com.ecommerce.identity.infrastructure.persistence.entity.AuthSessionEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AuthSessionDomainMapperImpl implements AuthSessionDomainMapper {

    @Override
    public AuthSession toDomain(AuthSessionEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String userId = null;
        AuthSessionStatus status = null;
        LocalDateTime createdAt = null;
        String sessionId = null;
        String ipAddress = null;
        String userAgent = null;
        LocalDateTime lastActiveAt = null;
        LocalDateTime expiresAt = null;

        userId = entityUserUserId( entity );
        status = mapStatus( entity.getStatus() );
        createdAt = entity.getCreatedAt();
        sessionId = entity.getSessionId();
        ipAddress = entity.getIpAddress();
        userAgent = entity.getUserAgent();
        lastActiveAt = entity.getLastActiveAt();
        expiresAt = entity.getExpiresAt();

        AuthSession authSession = new AuthSession( sessionId, userId, ipAddress, userAgent, status, createdAt, lastActiveAt, expiresAt );

        return authSession;
    }

    @Override
    public AuthSessionEntity toEntity(AuthSession domain, UserEntity userEntity) {
        if ( domain == null && userEntity == null ) {
            return null;
        }

        AuthSessionEntity.AuthSessionEntityBuilder authSessionEntity = AuthSessionEntity.builder();

        if ( domain != null ) {
            authSessionEntity.status( mapStatus( domain.getStatus() ) );
            authSessionEntity.createdAt( domain.getCreatedAt() );
            authSessionEntity.expiresAt( domain.getExpiresAt() );
            authSessionEntity.ipAddress( domain.getIpAddress() );
            authSessionEntity.lastActiveAt( domain.getLastActiveAt() );
            authSessionEntity.sessionId( domain.getSessionId() );
            authSessionEntity.userAgent( domain.getUserAgent() );
        }
        authSessionEntity.user( userEntity );

        return authSessionEntity.build();
    }

    private String entityUserUserId(AuthSessionEntity authSessionEntity) {
        UserEntity user = authSessionEntity.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUserId();
    }
}
