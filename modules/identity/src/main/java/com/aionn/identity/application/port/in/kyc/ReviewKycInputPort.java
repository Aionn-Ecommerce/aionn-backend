package com.aionn.identity.application.port.in.kyc;

import com.aionn.identity.application.dto.kyc.result.KycResult;
import com.aionn.identity.application.dto.kyc.command.ReviewKycCommand;

public interface ReviewKycInputPort {
    KycResult execute(ReviewKycCommand command);
}


