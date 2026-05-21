package com.aionn.identity.application.port.in.kyc;

import com.aionn.identity.application.dto.kyc.command.CancelKycCommand;

public interface CancelKycInputPort {
    void execute(CancelKycCommand command);
}


