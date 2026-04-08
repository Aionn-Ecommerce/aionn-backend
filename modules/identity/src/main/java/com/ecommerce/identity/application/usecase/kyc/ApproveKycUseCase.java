package com.ecommerce.identity.application.usecase.kyc;

import com.ecommerce.identity.application.dto.kyc.command.ApproveKycCommand;
import com.ecommerce.identity.application.dto.kyc.result.KycResult;
import com.ecommerce.identity.application.mapper.KycResultMapper;
import com.ecommerce.identity.application.port.in.kyc.ApproveKycInputPort;
import com.ecommerce.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApproveKycUseCase implements ApproveKycInputPort {

    private final KycService kycService;
    private final KycResultMapper kycResultMapper;

    @Override
    @Transactional
    public KycResult execute(ApproveKycCommand command) {
        var entity = kycService.approve(command.adminId(), command.kycId());
        return kycResultMapper.toResult(entity);
    }
}
