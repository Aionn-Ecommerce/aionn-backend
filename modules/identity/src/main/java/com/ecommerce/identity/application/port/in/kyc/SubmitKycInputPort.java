package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.SubmitKycCommand;
import com.ecommerce.identity.application.dto.kyc.KycResult;

public interface SubmitKycInputPort {
    KycResult execute(SubmitKycCommand command);
}