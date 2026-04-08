package com.ecommerce.identity.application.usecase.kyc;

import com.ecommerce.identity.application.dto.kyc.result.KycResult;
import com.ecommerce.identity.application.dto.kyc.command.ReviewKycCommand;
import com.ecommerce.identity.application.mapper.KycResultMapper;
import com.ecommerce.identity.application.port.in.kyc.ReviewKycInputPort;
import com.ecommerce.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewKycUseCase implements ReviewKycInputPort {

    private final KycService kycService;
    private final KycResultMapper kycResultMapper;

    @Override
    @Transactional
    public KycResult execute(ReviewKycCommand command) {
        var entity = kycService.review(command.adminId(), command.kycId(), command.note());
        return kycResultMapper.toResult(entity);
    }
}
