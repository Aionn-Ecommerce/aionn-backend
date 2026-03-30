package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.CompletePasswordResetCommand;

public interface CompletePasswordResetInputPort {
    void execute(CompletePasswordResetCommand command);
}