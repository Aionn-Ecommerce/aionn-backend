package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.CreateKycCommand;
import com.ecommerce.identity.application.dto.kyc.KycResult;

public interface CreateKycInputPort {
    KycResult execute(CreateKycCommand command);
}