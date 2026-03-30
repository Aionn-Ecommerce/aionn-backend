package com.ecommerce.identity.application.port.out.auth;

import java.time.LocalDateTime;

public interface AccessTokenIssuer {

    String issueAccessToken(String userId, String sessionId, LocalDateTime expiresAt);
}
