package com.ecommerce.identity.application.usecase.kyc;

import com.ecommerce.identity.adapter.rest.mapper.kyc.KycDtoMapper;
import com.ecommerce.identity.application.dto.kyc.ApproveKycCommand;
import com.ecommerce.identity.application.dto.kyc.KycResult;
import com.ecommerce.identity.application.port.in.kyc.ApproveKycInputPort;
import com.ecommerce.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApproveKycUseCase implements ApproveKycInputPort {

    private final KycService kycService;
    private final KycDtoMapper kycDtoMapper;

    @Override
    @Transactional
    public KycResult execute(ApproveKycCommand command) {
        var entity = kycService.approve(command.adminId(), command.kycId());
        return kycDtoMapper.toKycResult(entity);
    }
}
