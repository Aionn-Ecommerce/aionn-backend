package com.ecommerce.identity.application.usecase.kyc;

import com.ecommerce.identity.adapter.rest.mapper.kyc.KycDtoMapper;
import com.ecommerce.identity.application.dto.kyc.CreateKycCommand;
import com.ecommerce.identity.application.dto.kyc.KycResult;
import com.ecommerce.identity.application.port.in.kyc.CreateKycInputPort;
import com.ecommerce.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateKycUseCase implements CreateKycInputPort {

    private final KycService kycService;
    private final KycDtoMapper kycDtoMapper;

    @Override
    @Transactional
    public KycResult execute(CreateKycCommand command) {
        var entity = kycService.createKyc(command.userId(), command.docType());
        return kycDtoMapper.toKycResult(entity);
    }
}
