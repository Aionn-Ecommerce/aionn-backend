package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.SocialLink;
import com.ecommerce.identity.domain.valueobject.AuthProvider;
import com.ecommerce.identity.infrastructure.persistence.entity.SocialAccountEntity;
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
public class SocialLinkDomainMapperImpl implements SocialLinkDomainMapper {

    @Override
    public SocialLink toDomain(SocialAccountEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String userId = null;
        String socialAccountId = null;
        String providerUserId = null;
        LocalDateTime createdAt = null;

        userId = entityUserUserId( entity );
        socialAccountId = entity.getSocialAccountId();
        providerUserId = entity.getProviderUserId();
        createdAt = entity.getCreatedAt();

        AuthProvider provider = AuthProvider.valueOf(entity.getProvider());

        SocialLink socialLink = new SocialLink( socialAccountId, userId, provider, providerUserId, createdAt );

        return socialLink;
    }

    @Override
    public SocialAccountEntity toEntity(SocialLink domain, UserEntity userEntity) {
        if ( domain == null && userEntity == null ) {
            return null;
        }

        SocialAccountEntity.SocialAccountEntityBuilder socialAccountEntity = SocialAccountEntity.builder();

        if ( domain != null ) {
            socialAccountEntity.createdAt( domain.createdAt() );
            socialAccountEntity.providerUserId( domain.providerUserId() );
            socialAccountEntity.socialAccountId( domain.socialAccountId() );
        }
        socialAccountEntity.user( userEntity );
        socialAccountEntity.provider( domain.provider().name() );

        return socialAccountEntity.build();
    }

    private String entityUserUserId(SocialAccountEntity socialAccountEntity) {
        UserEntity user = socialAccountEntity.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUserId();
    }
}
