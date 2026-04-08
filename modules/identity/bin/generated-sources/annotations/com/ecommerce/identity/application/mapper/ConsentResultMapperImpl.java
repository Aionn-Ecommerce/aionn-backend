package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.consent.result.ConsentResult;
import com.ecommerce.identity.infrastructure.persistence.entity.UserConsentEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ConsentResultMapperImpl implements ConsentResultMapper {

    @Override
    public ConsentResult toResult(UserConsentEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String userId = null;
        String consentId = null;
        String consentType = null;
        String version = null;
        LocalDateTime agreedAt = null;
        LocalDateTime revokedAt = null;
        String ipAddress = null;

        userId = entityUserUserId( entity );
        consentId = entity.getConsentId();
        consentType = entity.getConsentType();
        version = entity.getVersion();
        agreedAt = entity.getAgreedAt();
        revokedAt = entity.getRevokedAt();
        ipAddress = entity.getIpAddress();

        boolean agreed = entity.getRevokedAt() == null;

        ConsentResult consentResult = new ConsentResult( consentId, userId, consentType, version, agreed, agreedAt, revokedAt, ipAddress );

        return consentResult;
    }

    @Override
    public List<ConsentResult> toResults(List<UserConsentEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ConsentResult> list = new ArrayList<ConsentResult>( entities.size() );
        for ( UserConsentEntity userConsentEntity : entities ) {
            list.add( toResult( userConsentEntity ) );
        }

        return list;
    }

    private String entityUserUserId(UserConsentEntity userConsentEntity) {
        UserEntity user = userConsentEntity.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUserId();
    }
}
