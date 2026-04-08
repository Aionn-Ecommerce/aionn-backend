package com.ecommerce.identity.application.usecase.consent;

import com.ecommerce.identity.application.dto.consent.result.ConsentResult;
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

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<ConsentResult> execute(String userId) {
        return consentService.listMy(userId);
    }
}
