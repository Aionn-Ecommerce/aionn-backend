package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.result.KycResult;
import com.ecommerce.identity.application.dto.kyc.command.CreateKycCommand;

public interface CreateKycInputPort {
    KycResult execute(CreateKycCommand command);
}

