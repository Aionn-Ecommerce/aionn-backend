package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.ReviewKycCommand;
import com.ecommerce.identity.application.dto.kyc.KycResult;

public interface ReviewKycInputPort {
    KycResult execute(ReviewKycCommand command);
}