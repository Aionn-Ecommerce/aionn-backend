package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.LogoutCommand;

public interface LogoutInputPort {

    void execute(LogoutCommand command);
}
