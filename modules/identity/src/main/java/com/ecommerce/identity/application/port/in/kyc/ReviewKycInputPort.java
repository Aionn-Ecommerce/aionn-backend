package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.result.KycResult;
import com.ecommerce.identity.application.dto.kyc.command.ReviewKycCommand;

public interface ReviewKycInputPort {
    KycResult execute(ReviewKycCommand command);
}

