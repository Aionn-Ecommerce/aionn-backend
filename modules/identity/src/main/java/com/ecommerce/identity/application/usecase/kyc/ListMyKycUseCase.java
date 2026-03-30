package com.ecommerce.identity.application.usecase.kyc;

import com.ecommerce.identity.adapter.rest.mapper.kyc.KycDtoMapper;
import com.ecommerce.identity.application.dto.kyc.KycResult;
import com.ecommerce.identity.application.port.in.kyc.ListMyKycQueryPort;
import com.ecommerce.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMyKycUseCase implements ListMyKycQueryPort {

    private final KycService kycService;
    private final KycDtoMapper kycDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<KycResult> execute(String userId) {
        var entities = kycService.listMy(userId);
        return kycDtoMapper.toKycResults(entities);
    }
}
