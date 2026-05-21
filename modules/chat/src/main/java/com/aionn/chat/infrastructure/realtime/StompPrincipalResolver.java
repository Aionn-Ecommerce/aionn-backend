package com.aionn.chat.infrastructure.realtime;

import com.aionn.identity.application.port.out.auth.AuthSessionPersistencePort;
import com.aionn.identity.domain.model.AuthSession;
import com.aionn.identity.domain.valueobject.AuthSessionStatus;
import com.aionn.identity.infrastructure.auth.AccessTokenIssuerAdapter;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Reuses the JWT validation logic from the HTTP layer to authenticate STOMP
 * CONNECT frames. We deliberately keep this in the chat module instead of
 * importing the HTTP filter so we avoid pulling servlet plumbing into the
 * STOMP path.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompPrincipalResolver {

    private static final String BEARER = "Bearer ";

    private final AccessTokenIssuerAdapter tokenIssuer;
    private final AuthSessionPersistencePort authSessionPersistencePort;

    public String resolveUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER)) {
            return null;
        }
        String token = authorizationHeader.substring(BEARER.length()).trim();
        Optional<Claims> parsed = tokenIssuer.parse(token);
        if (parsed.isEmpty()) {
            return null;
        }
        Claims claims = parsed.get();
        String sessionId = claims.get("sid", String.class);
        String userId = claims.getSubject();
        if (sessionId == null || userId == null) {
            return null;
        }
        Optional<AuthSession> sessionOpt = authSessionPersistencePort.findById(sessionId);
        if (sessionOpt.isEmpty()
                || !AuthSessionStatus.ACTIVE.equals(sessionOpt.get().getStatus())
                || sessionOpt.get().isExpired()
                || !userId.equals(sessionOpt.get().getUserId())) {
            return null;
        }
        return userId;
    }
}

