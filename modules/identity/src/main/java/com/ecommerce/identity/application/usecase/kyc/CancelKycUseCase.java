package com.ecommerce.identity.application.usecase.kyc;

import com.ecommerce.identity.application.dto.kyc.command.CancelKycCommand;
import com.ecommerce.identity.application.port.in.kyc.CancelKycInputPort;
import com.ecommerce.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelKycUseCase implements CancelKycInputPort {

    private final KycService kycService;

    @Override
    @Transactional
    public void execute(CancelKycCommand command) {
        kycService.cancel(command.userId(), command.kycId());
    }
}


