package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.command.LogoutAllCommand;
import com.ecommerce.identity.application.dto.auth.result.LogoutAllResult;

public interface LogoutAllInputPort {

    LogoutAllResult execute(LogoutAllCommand command);
}


