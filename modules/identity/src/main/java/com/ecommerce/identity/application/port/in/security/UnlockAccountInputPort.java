package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.UnlockAccountCommand;

public interface UnlockAccountInputPort {
    void execute(UnlockAccountCommand command);
}