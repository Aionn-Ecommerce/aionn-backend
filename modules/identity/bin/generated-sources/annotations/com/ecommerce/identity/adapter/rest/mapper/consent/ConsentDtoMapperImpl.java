package com.ecommerce.identity.adapter.rest.mapper.consent;

import com.ecommerce.identity.adapter.rest.dto.consent.ConsentResponse;
import com.ecommerce.identity.adapter.rest.dto.consent.MarketingConsentRequest;
import com.ecommerce.identity.adapter.rest.dto.consent.TermsConsentRequest;
import com.ecommerce.identity.application.dto.consent.AgreePrivacyCommand;
import com.ecommerce.identity.application.dto.consent.AgreeTermsCommand;
import com.ecommerce.identity.application.dto.consent.ConsentResult;
import com.ecommerce.identity.application.dto.consent.UpdateMarketingConsentCommand;
import com.ecommerce.identity.infrastructure.persistence.entity.UserConsentEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-31T09:49:08+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ConsentDtoMapperImpl implements ConsentDtoMapper {

    @Override
    public AgreeTermsCommand toTermsConsentCommand(String userId, String clientIp, TermsConsentRequest request) {
        if ( userId == null && clientIp == null && request == null ) {
            return null;
        }

        String version = null;
        if ( request != null ) {
            version = request.version();
        }
        String userId1 = null;
        userId1 = userId;
        String clientIp1 = null;
        clientIp1 = clientIp;

        AgreeTermsCommand agreeTermsCommand = new AgreeTermsCommand( userId1, version, clientIp1 );

        return agreeTermsCommand;
    }

    @Override
    public AgreePrivacyCommand toPrivacyConsentCommand(String userId, String clientIp, TermsConsentRequest request) {
        if ( userId == null && clientIp == null && request == null ) {
            return null;
        }

        String version = null;
        if ( request != null ) {
            version = request.version();
        }
        String userId1 = null;
        userId1 = userId;
        String clientIp1 = null;
        clientIp1 = clientIp;

        AgreePrivacyCommand agreePrivacyCommand = new AgreePrivacyCommand( userId1, version, clientIp1 );

        return agreePrivacyCommand;
    }

    @Override
    public UpdateMarketingConsentCommand toMarketingConsentCommand(String userId, String clientIp, MarketingConsentRequest request) {
        if ( userId == null && clientIp == null && request == null ) {
            return null;
        }

        boolean subscribed = false;
        if ( request != null ) {
            subscribed = request.subscribed();
        }
        String userId1 = null;
        userId1 = userId;
        String clientIp1 = null;
        clientIp1 = clientIp;

        UpdateMarketingConsentCommand updateMarketingConsentCommand = new UpdateMarketingConsentCommand( userId1, subscribed, clientIp1 );

        return updateMarketingConsentCommand;
    }

    @Override
    public ConsentResult toConsentResult(UserConsentEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String consentId = null;
        String consentType = null;
        String version = null;
        LocalDateTime agreedAt = null;
        LocalDateTime revokedAt = null;
        String ipAddress = null;

        consentId = entity.getConsentId();
        consentType = entity.getConsentType();
        version = entity.getVersion();
        agreedAt = entity.getAgreedAt();
        revokedAt = entity.getRevokedAt();
        ipAddress = entity.getIpAddress();

        ConsentResult consentResult = new ConsentResult( consentId, consentType, version, agreedAt, revokedAt, ipAddress );

        return consentResult;
    }

    @Override
    public List<ConsentResult> toConsentResults(List<UserConsentEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ConsentResult> list = new ArrayList<ConsentResult>( entities.size() );
        for ( UserConsentEntity userConsentEntity : entities ) {
            list.add( toConsentResult( userConsentEntity ) );
        }

        return list;
    }

    @Override
    public ConsentResponse toResponse(ConsentResult result) {
        if ( result == null ) {
            return null;
        }

        String consentId = null;
        String consentType = null;
        String version = null;
        LocalDateTime agreedAt = null;
        LocalDateTime revokedAt = null;
        String ipAddress = null;

        consentId = result.consentId();
        consentType = result.consentType();
        version = result.version();
        agreedAt = result.agreedAt();
        revokedAt = result.revokedAt();
        ipAddress = result.ipAddress();

        ConsentResponse consentResponse = new ConsentResponse( consentId, consentType, version, agreedAt, revokedAt, ipAddress );

        return consentResponse;
    }

    @Override
    public List<ConsentResponse> toResponses(List<ConsentResult> results) {
        if ( results == null ) {
            return null;
        }

        List<ConsentResponse> list = new ArrayList<ConsentResponse>( results.size() );
        for ( ConsentResult consentResult : results ) {
            list.add( toResponse( consentResult ) );
        }

        return list;
    }
}
