package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.id.UserId;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IdentityUserMapper {

    @Mapping(target = "id", source = "userId")
    IdentityUser toDomain(UserEntity entity);

    @Mapping(target = "userId", source = "id.value")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "mfaEnabled", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    UserEntity toEntity(IdentityUser user);

    default UserId map(String value) {
        return value != null ? UserId.of(value) : null;
    }
}
