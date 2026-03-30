package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.ApproveKycCommand;
import com.ecommerce.identity.application.dto.kyc.KycResult;

public interface ApproveKycInputPort {
    KycResult execute(ApproveKycCommand command);
}