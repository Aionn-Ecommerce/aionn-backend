package com.aionn.identity.application.port.in.auth;

import com.aionn.identity.application.dto.auth.command.RefreshTokenCommand;
import com.aionn.identity.application.dto.auth.result.RefreshAccessTokenResult;

/**
 * @deprecated Use {@link RefreshTokenInputPort} which is the actual port the
 *             refresh use case implements.
 */
@Deprecated(since = "2.0", forRemoval = true)
public interface RefreshAccessTokenInputPort {

    RefreshAccessTokenResult execute(RefreshTokenCommand command);
}

