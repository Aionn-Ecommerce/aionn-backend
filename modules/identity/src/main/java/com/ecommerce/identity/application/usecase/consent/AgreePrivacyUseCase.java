package com.ecommerce.identity.application.usecase.consent;

import com.ecommerce.identity.adapter.rest.mapper.consent.ConsentDtoMapper;
import com.ecommerce.identity.application.dto.consent.AgreePrivacyCommand;
import com.ecommerce.identity.application.dto.consent.ConsentResult;
import com.ecommerce.identity.application.port.in.consent.AgreePrivacyInputPort;
import com.ecommerce.identity.application.service.ConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgreePrivacyUseCase implements AgreePrivacyInputPort {

    private final ConsentService consentService;
    private final ConsentDtoMapper consentDtoMapper;

    @Override
    @Transactional
    public ConsentResult execute(AgreePrivacyCommand command) {
        var entity = consentService.agreePrivacy(command.userId(), command.version(), command.clientIp());
        return consentDtoMapper.toConsentResult(entity);
    }
}
