package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.AuthSession;
import com.ecommerce.identity.domain.valueobject.AuthSessionStatus;
import com.ecommerce.identity.infrastructure.persistence.entity.AuthSessionEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthSessionDomainMapper {

    public AuthSession toDomain(AuthSessionEntity entity) {
        return new AuthSession(
                entity.getSessionId(),
                entity.getUser().getUserId(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                AuthSessionStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getLastActiveAt(),
                entity.getExpiresAt());
    }

    public AuthSessionEntity toEntity(AuthSession domain, UserEntity userEntity) {
        return AuthSessionEntity.builder()
                .sessionId(domain.getSessionId())
                .user(userEntity)
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .status(domain.getStatus().name())
                .createdAt(domain.getCreatedAt())
                .lastActiveAt(domain.getLastActiveAt())
                .expiresAt(domain.getExpiresAt())
                .build();
    }
}
