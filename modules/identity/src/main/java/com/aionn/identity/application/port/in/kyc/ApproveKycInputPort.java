package com.aionn.identity.application.port.in.kyc;

import com.aionn.identity.application.dto.kyc.command.ApproveKycCommand;
import com.aionn.identity.application.dto.kyc.result.KycResult;

public interface ApproveKycInputPort {
    KycResult execute(ApproveKycCommand command);
}


