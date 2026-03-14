package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import java.time.LocalDateTime;

public final class IdentityUserMapper {

    private IdentityUserMapper() {
    }

    public static IdentityUser toDomain(UserEntity entity) {
        LocalDateTime createdAt = entity.getCreatedAt() == null ? LocalDateTime.now() : entity.getCreatedAt();
        String displayName = entity.getDisplayName() == null || entity.getDisplayName().isBlank()
                ? "unknown"
                : entity.getDisplayName();

        return new IdentityUser(entity.getUserId(), entity.getEmail(), displayName, createdAt);
    }
}