package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.result.RefreshAccessTokenResult;

public interface RefreshAccessTokenInputPort {

    RefreshAccessTokenResult refreshAccessToken(String userId, String sessionId);
}


