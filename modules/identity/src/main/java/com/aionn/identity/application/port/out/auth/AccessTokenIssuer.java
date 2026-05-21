package com.aionn.identity.application.port.out.auth;

import java.time.LocalDateTime;
import java.util.Set;

public interface AccessTokenIssuer {
	String issueAccessToken(String userId, String sessionId, LocalDateTime expiresAt, Set<String> roles);
}
