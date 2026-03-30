package com.ecommerce.identity.application.usecase.consent;

import com.ecommerce.identity.adapter.rest.mapper.consent.ConsentDtoMapper;
import com.ecommerce.identity.application.dto.consent.ConsentResult;
import com.ecommerce.identity.application.port.in.consent.GetMyConsentsQueryPort;
import com.ecommerce.identity.application.service.ConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMyConsentsUseCase implements GetMyConsentsQueryPort {

    private final ConsentService consentService;
    private final ConsentDtoMapper consentDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ConsentResult> execute(String userId) {
        var entities = consentService.listMy(userId);
        return consentDtoMapper.toConsentResults(entities);
    }
}
