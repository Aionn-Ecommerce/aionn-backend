package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.SocialLoginCommand;
import com.ecommerce.identity.application.dto.auth.SocialLoginResult;

public interface SocialLoginInputPort {

    SocialLoginResult socialLogin(SocialLoginCommand command);
}
