package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.CancelKycCommand;

public interface CancelKycInputPort {
    void execute(CancelKycCommand command);
}