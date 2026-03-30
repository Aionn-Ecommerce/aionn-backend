package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.LoginCommand;
import com.ecommerce.identity.application.dto.auth.LoginResult;

public interface LoginInputPort {

    LoginResult execute(LoginCommand command);
}
