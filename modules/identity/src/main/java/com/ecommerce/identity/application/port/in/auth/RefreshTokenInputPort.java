package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.result.RefreshAccessTokenResult;
import com.ecommerce.identity.application.dto.auth.command.RefreshTokenCommand;

public interface RefreshTokenInputPort {

    RefreshAccessTokenResult execute(RefreshTokenCommand command);
}
