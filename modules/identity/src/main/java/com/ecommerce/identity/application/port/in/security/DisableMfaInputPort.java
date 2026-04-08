package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.result.MfaResult;
import com.ecommerce.identity.application.dto.security.command.DisableMfaCommand;

public interface DisableMfaInputPort {
    MfaResult execute(DisableMfaCommand command);
}


