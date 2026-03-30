package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.LoginResult;
import com.ecommerce.identity.application.dto.auth.SocialLoginCommand;

public interface SocialAuthInputPort {

    LoginResult execute(SocialLoginCommand command);
}
