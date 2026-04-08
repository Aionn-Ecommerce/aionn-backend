package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.result.PasswordResetResult;
import com.ecommerce.identity.application.dto.security.command.RequestPasswordResetCommand;

public interface RequestPasswordResetInputPort {
    PasswordResetResult execute(RequestPasswordResetCommand command);
}

