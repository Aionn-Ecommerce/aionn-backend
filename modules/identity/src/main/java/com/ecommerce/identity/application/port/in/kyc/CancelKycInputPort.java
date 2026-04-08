package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.command.CancelKycCommand;

public interface CancelKycInputPort {
    void execute(CancelKycCommand command);
}

