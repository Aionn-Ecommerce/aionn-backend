package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.SocialLink;
import com.ecommerce.identity.domain.valueobject.AuthProvider;
import com.ecommerce.identity.infrastructure.persistence.entity.SocialAccountEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SocialLinkDomainMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "provider", expression = "java(AuthProvider.valueOf(entity.getProvider()))")
    SocialLink toDomain(SocialAccountEntity entity);

    @Mapping(target = "user", source = "userEntity")
    @Mapping(target = "provider", expression = "java(domain.provider().name())")
    @Mapping(target = "createdAt", source = "domain.createdAt")
    SocialAccountEntity toEntity(SocialLink domain, UserEntity userEntity);
}
