package com.ecommerce.identity.application.usecase.kyc;

import com.ecommerce.identity.adapter.rest.mapper.kyc.KycDtoMapper;
import com.ecommerce.identity.application.dto.kyc.KycResult;
import com.ecommerce.identity.application.dto.kyc.RejectKycCommand;
import com.ecommerce.identity.application.port.in.kyc.RejectKycInputPort;
import com.ecommerce.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RejectKycUseCase implements RejectKycInputPort {

    private final KycService kycService;
    private final KycDtoMapper kycDtoMapper;

    @Override
    @Transactional
    public KycResult execute(RejectKycCommand command) {
        var entity = kycService.reject(command.adminId(), command.kycId(), command.reason());
        return kycDtoMapper.toKycResult(entity);
    }
}
