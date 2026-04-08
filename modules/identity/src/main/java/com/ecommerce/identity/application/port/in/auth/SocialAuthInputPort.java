package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.result.SocialLoginResult;
import com.ecommerce.identity.application.dto.auth.command.SocialLoginCommand;

public interface SocialAuthInputPort {

    SocialLoginResult execute(SocialLoginCommand command);
}
