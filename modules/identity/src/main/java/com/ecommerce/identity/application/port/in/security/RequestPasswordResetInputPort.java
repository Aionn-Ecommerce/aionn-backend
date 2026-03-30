package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.RequestPasswordResetCommand;
import com.ecommerce.identity.application.dto.security.PasswordResetResult;

public interface RequestPasswordResetInputPort {
    PasswordResetResult execute(RequestPasswordResetCommand command);
}