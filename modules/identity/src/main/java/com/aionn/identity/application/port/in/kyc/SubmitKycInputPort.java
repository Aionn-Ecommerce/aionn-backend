package com.aionn.identity.application.port.in.kyc;

import com.aionn.identity.application.dto.kyc.result.KycResult;
import com.aionn.identity.application.dto.kyc.command.SubmitKycCommand;

public interface SubmitKycInputPort {
    KycResult execute(SubmitKycCommand command);
}


