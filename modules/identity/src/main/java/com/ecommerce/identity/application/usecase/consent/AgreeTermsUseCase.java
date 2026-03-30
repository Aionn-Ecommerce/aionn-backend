package com.ecommerce.identity.application.usecase.consent;

import com.ecommerce.identity.adapter.rest.mapper.consent.ConsentDtoMapper;
import com.ecommerce.identity.application.dto.consent.AgreeTermsCommand;
import com.ecommerce.identity.application.dto.consent.ConsentResult;
import com.ecommerce.identity.application.port.in.consent.AgreeTermsInputPort;
import com.ecommerce.identity.application.service.ConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgreeTermsUseCase implements AgreeTermsInputPort {

    private final ConsentService consentService;
    private final ConsentDtoMapper consentDtoMapper;

    @Override
    @Transactional
    public ConsentResult execute(AgreeTermsCommand command) {
        var entity = consentService.agreeTerms(command.userId(), command.version(), command.clientIp());
        return consentDtoMapper.toConsentResult(entity);
    }
}
