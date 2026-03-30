package com.ecommerce.identity.adapter.rest.mapper.consent;

import com.ecommerce.identity.adapter.rest.dto.consent.ConsentResponse;
import com.ecommerce.identity.adapter.rest.dto.consent.MarketingConsentRequest;
import com.ecommerce.identity.adapter.rest.dto.consent.TermsConsentRequest;
import com.ecommerce.identity.application.dto.consent.AgreePrivacyCommand;
import com.ecommerce.identity.application.dto.consent.AgreeTermsCommand;
import com.ecommerce.identity.application.dto.consent.ConsentResult;
import com.ecommerce.identity.application.dto.consent.UpdateMarketingConsentCommand;
import com.ecommerce.identity.infrastructure.persistence.entity.UserConsentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConsentDtoMapper {

    // Request -> Command
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "version", source = "request.version")
    @Mapping(target = "clientIp", source = "clientIp")
    AgreeTermsCommand toTermsConsentCommand(String userId, String clientIp, TermsConsentRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "version", source = "request.version")
    @Mapping(target = "clientIp", source = "clientIp")
    AgreePrivacyCommand toPrivacyConsentCommand(String userId, String clientIp, TermsConsentRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "subscribed", source = "request.subscribed")
    @Mapping(target = "clientIp", source = "clientIp")
    UpdateMarketingConsentCommand toMarketingConsentCommand(String userId, String clientIp,
            MarketingConsentRequest request);

    // Entity -> Result
    @Mapping(target = "consentId", source = "consentId")
    @Mapping(target = "consentType", source = "consentType")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "agreedAt", source = "agreedAt")
    @Mapping(target = "revokedAt", source = "revokedAt")
    @Mapping(target = "ipAddress", source = "ipAddress")
    ConsentResult toConsentResult(UserConsentEntity entity);

    List<ConsentResult> toConsentResults(List<UserConsentEntity> entities);

    // Result -> Response
    @Mapping(target = "consentId", source = "consentId")
    @Mapping(target = "consentType", source = "consentType")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "agreedAt", source = "agreedAt")
    @Mapping(target = "revokedAt", source = "revokedAt")
    @Mapping(target = "ipAddress", source = "ipAddress")
    ConsentResponse toResponse(ConsentResult result);

    List<ConsentResponse> toResponses(List<ConsentResult> results);
}
