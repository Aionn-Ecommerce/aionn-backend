package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.command.EnableMfaCommand;
import com.ecommerce.identity.application.dto.security.result.MfaResult;

public interface EnableMfaInputPort {
    MfaResult execute(EnableMfaCommand command);
}


