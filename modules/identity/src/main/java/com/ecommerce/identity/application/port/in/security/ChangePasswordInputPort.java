package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.command.ChangePasswordCommand;

public interface ChangePasswordInputPort {
    void execute(ChangePasswordCommand command);
}


