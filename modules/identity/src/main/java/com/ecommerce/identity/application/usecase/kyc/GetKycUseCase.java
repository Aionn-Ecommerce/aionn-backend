package com.ecommerce.identity.application.usecase.kyc;

import com.ecommerce.identity.adapter.rest.mapper.kyc.KycDtoMapper;
import com.ecommerce.identity.application.dto.kyc.GetKycQuery;
import com.ecommerce.identity.application.dto.kyc.KycResult;
import com.ecommerce.identity.application.port.in.kyc.GetKycQueryPort;
import com.ecommerce.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetKycUseCase implements GetKycQueryPort {

    private final KycService kycService;
    private final KycDtoMapper kycDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public KycResult execute(GetKycQuery query) {
        var entity = kycService.get(query.userId(), query.kycId());
        return kycDtoMapper.toKycResult(entity);
    }
}
