package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.RejectKycCommand;
import com.ecommerce.identity.application.dto.kyc.KycResult;

public interface RejectKycInputPort {
    KycResult execute(RejectKycCommand command);
}