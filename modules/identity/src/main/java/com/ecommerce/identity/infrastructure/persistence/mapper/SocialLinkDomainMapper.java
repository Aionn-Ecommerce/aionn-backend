package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.SocialLink;
import com.ecommerce.identity.domain.valueobject.AuthProvider;
import com.ecommerce.identity.infrastructure.persistence.entity.SocialAccountEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class SocialLinkDomainMapper {

    public SocialLink toDomain(SocialAccountEntity entity) {
        return new SocialLink(
                entity.getSocialAccountId(),
                entity.getUser().getUserId(),
                AuthProvider.valueOf(entity.getProvider()),
                entity.getProviderUserId(),
                entity.getCreatedAt());
    }

    public SocialAccountEntity toEntity(SocialLink domain, UserEntity userEntity) {
        return SocialAccountEntity.builder()
                .socialAccountId(domain.getSocialAccountId())
                .user(userEntity)
                .provider(domain.getProvider().name())
                .providerUserId(domain.getProviderUserId())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
