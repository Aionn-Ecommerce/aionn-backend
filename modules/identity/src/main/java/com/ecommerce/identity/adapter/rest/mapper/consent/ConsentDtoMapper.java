package com.ecommerce.identity.adapter.rest.mapper.consent;

import com.ecommerce.identity.adapter.rest.dto.consent.ConsentResponse;
import com.ecommerce.identity.adapter.rest.dto.consent.MarketingConsentRequest;
import com.ecommerce.identity.adapter.rest.dto.consent.TermsConsentRequest;
import com.ecommerce.identity.application.dto.consent.command.AgreePrivacyCommand;
import com.ecommerce.identity.application.dto.consent.command.AgreeTermsCommand;
import com.ecommerce.identity.application.dto.consent.result.ConsentResult;
import com.ecommerce.identity.application.dto.consent.command.UpdateMarketingConsentCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConsentDtoMapper {

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

    ConsentResponse toResponse(ConsentResult result);

    List<ConsentResponse> toResponses(List<ConsentResult> results);
}
