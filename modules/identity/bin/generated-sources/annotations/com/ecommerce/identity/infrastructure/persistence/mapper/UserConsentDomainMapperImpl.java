package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.UserConsent;
import com.ecommerce.identity.domain.valueobject.ConsentType;
import com.ecommerce.identity.infrastructure.persistence.entity.UserConsentEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:30:33+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserConsentDomainMapperImpl implements UserConsentDomainMapper {

    @Override
    public UserConsentEntity toEntity(UserConsent domain) {
        if ( domain == null ) {
            return null;
        }

        UserConsentEntity.UserConsentEntityBuilder userConsentEntity = UserConsentEntity.builder();

        userConsentEntity.consentId( domain.getId() );
        userConsentEntity.version( domain.getVersion() );
        userConsentEntity.agreedAt( domain.getAgreedAt() );
        userConsentEntity.revokedAt( domain.getRevokedAt() );
        userConsentEntity.ipAddress( domain.getIpAddress() );

        userConsentEntity.consentType( domain.getConsentType().name() );

        return userConsentEntity.build();
    }

    @Override
    public UserConsent toDomain(UserConsentEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserConsent.UserConsentBuilder userConsent = UserConsent.builder();

        userConsent.id( entity.getConsentId() );
        userConsent.userId( entityUserUserId( entity ) );
        userConsent.version( entity.getVersion() );
        userConsent.agreedAt( entity.getAgreedAt() );
        userConsent.revokedAt( entity.getRevokedAt() );
        userConsent.ipAddress( entity.getIpAddress() );

        userConsent.consentType( ConsentType.valueOf(entity.getConsentType()) );
        userConsent.granted( entity.getRevokedAt() == null );

        return userConsent.build();
    }

    private String entityUserUserId(UserConsentEntity userConsentEntity) {
        UserEntity user = userConsentEntity.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUserId();
    }
}
