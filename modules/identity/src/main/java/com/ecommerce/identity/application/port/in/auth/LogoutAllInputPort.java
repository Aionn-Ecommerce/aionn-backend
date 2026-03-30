package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.LogoutAllCommand;
import com.ecommerce.identity.application.dto.auth.LogoutAllResult;

public interface LogoutAllInputPort {

    LogoutAllResult execute(LogoutAllCommand command);
}
