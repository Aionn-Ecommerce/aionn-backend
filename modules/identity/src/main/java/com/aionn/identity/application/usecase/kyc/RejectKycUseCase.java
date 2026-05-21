package com.aionn.identity.application.usecase.kyc;

import com.aionn.identity.application.dto.kyc.result.KycResult;
import com.aionn.identity.application.dto.kyc.command.RejectKycCommand;
import com.aionn.identity.application.mapper.KycResultMapper;
import com.aionn.identity.application.port.in.kyc.RejectKycInputPort;
import com.aionn.identity.application.service.KycService;
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

