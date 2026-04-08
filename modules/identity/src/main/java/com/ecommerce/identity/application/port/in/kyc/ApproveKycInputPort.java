package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.command.ApproveKycCommand;
import com.ecommerce.identity.application.dto.kyc.result.KycResult;

public interface ApproveKycInputPort {
    KycResult execute(ApproveKycCommand command);
}

