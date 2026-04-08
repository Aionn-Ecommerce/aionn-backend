package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.command.SocialLoginCommand;
import com.ecommerce.identity.application.dto.auth.result.SocialLoginResult;

public interface SocialLoginInputPort {

    SocialLoginResult socialLogin(SocialLoginCommand command);
}


