package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.DisableMfaCommand;
import com.ecommerce.identity.application.dto.security.MfaResult;

public interface DisableMfaInputPort {
    MfaResult execute(DisableMfaCommand command);
}