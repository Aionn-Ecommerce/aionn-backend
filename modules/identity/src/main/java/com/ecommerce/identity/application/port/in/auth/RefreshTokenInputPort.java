package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.LoginResult;
import com.ecommerce.identity.application.dto.auth.RefreshAccessTokenResult;
import com.ecommerce.identity.application.dto.auth.RefreshTokenCommand;

public interface RefreshTokenInputPort {

    RefreshAccessTokenResult refreshToken(String userId, String sessionId);

    LoginResult execute(RefreshTokenCommand command);
}
