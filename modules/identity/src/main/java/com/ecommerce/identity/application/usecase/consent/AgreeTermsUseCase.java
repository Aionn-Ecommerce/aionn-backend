package com.ecommerce.identity.application.usecase.consent;

import com.ecommerce.identity.application.dto.consent.command.AgreeTermsCommand;
import com.ecommerce.identity.application.dto.consent.result.ConsentResult;
import com.ecommerce.identity.application.port.in.consent.AgreeTermsInputPort;
import com.ecommerce.identity.application.service.ConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgreeTermsUseCase implements AgreeTermsInputPort {

    private final ConsentService consentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsentResult execute(AgreeTermsCommand command) {
        return consentService.agreeTerms(command.userId(), command.version(), command.clientIp());
    }
}
