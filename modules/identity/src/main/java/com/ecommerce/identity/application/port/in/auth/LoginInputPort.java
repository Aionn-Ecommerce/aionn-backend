package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.command.LoginCommand;
import com.ecommerce.identity.application.dto.auth.result.LoginResult;

public interface LoginInputPort {

    LoginResult execute(LoginCommand command);
}
