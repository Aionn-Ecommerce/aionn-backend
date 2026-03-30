package com.ecommerce.identity.application.usecase.consent;

import com.ecommerce.identity.adapter.rest.mapper.consent.ConsentDtoMapper;
import com.ecommerce.identity.application.dto.consent.ConsentResult;
import com.ecommerce.identity.application.dto.consent.UpdateMarketingConsentCommand;
import com.ecommerce.identity.application.port.in.consent.UpdateMarketingConsentInputPort;
import com.ecommerce.identity.application.service.ConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateMarketingConsentUseCase implements UpdateMarketingConsentInputPort {

    private final ConsentService consentService;
    private final ConsentDtoMapper consentDtoMapper;

    @Override
    @Transactional
    public ConsentResult execute(UpdateMarketingConsentCommand command) {
        var entity = consentService.updateMarketing(command.userId(), command.subscribed(), command.clientIp());
        return consentDtoMapper.toConsentResult(entity);
    }
}
