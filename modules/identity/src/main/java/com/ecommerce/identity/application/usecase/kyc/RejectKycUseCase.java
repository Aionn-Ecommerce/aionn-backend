package com.ecommerce.identity.application.usecase.kyc;

import com.ecommerce.identity.application.dto.kyc.result.KycResult;
import com.ecommerce.identity.application.dto.kyc.command.RejectKycCommand;
import com.ecommerce.identity.application.mapper.KycResultMapper;
import com.ecommerce.identity.application.port.in.kyc.RejectKycInputPort;
import com.ecommerce.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RejectKycUseCase implements RejectKycInputPort {

    private final KycService kycService;
    private final KycResultMapper kycResultMapper;

    @Override
    @Transactional
    public KycResult execute(RejectKycCommand command) {
        var entity = kycService.reject(command.adminId(), command.kycId(), command.reason());
        return kycResultMapper.toResult(entity);
    }
}
