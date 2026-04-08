package com.ecommerce.identity.application.usecase.consent;

import com.ecommerce.identity.application.dto.consent.command.AgreePrivacyCommand;
import com.ecommerce.identity.application.dto.consent.result.ConsentResult;
import com.ecommerce.identity.application.port.in.consent.AgreePrivacyInputPort;
import com.ecommerce.identity.application.service.ConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgreePrivacyUseCase implements AgreePrivacyInputPort {

    private final ConsentService consentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsentResult execute(AgreePrivacyCommand command) {
        return consentService.agreePrivacy(command.userId(), command.version(), command.clientIp());
    }
}
