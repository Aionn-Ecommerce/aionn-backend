package com.ecommerce.identity.infrastructure.auth;

import com.ecommerce.identity.application.port.out.auth.AccessTokenIssuer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Component
public class AccessTokenIssuerAdapter implements AccessTokenIssuer {

    @Override
    public String issueAccessToken(String userId, String sessionId, LocalDateTime expiresAt) {
        String payload = userId + ":" + sessionId + ":" + expiresAt + ":" + UUID.randomUUID();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}
