package com.aionn.chat.infrastructure.realtime;

import com.aionn.identity.application.port.out.auth.AccessTokenClaims;
import com.aionn.identity.application.port.out.auth.AccessTokenIssuerPort;
import com.aionn.identity.application.port.out.auth.AuthSessionPersistencePort;
import com.aionn.identity.domain.model.AuthSession;
import com.aionn.identity.domain.valueobject.AuthSessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompPrincipalResolver {

    private static final String BEARER = "Bearer ";

    private final AccessTokenIssuerPort tokenIssuer;
    private final AuthSessionPersistencePort authSessionPersistencePort;

    public String resolveUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER)) {
            return null;
        }
        String token = authorizationHeader.substring(BEARER.length()).trim();
        Optional<AccessTokenClaims> parsed = tokenIssuer.parseClaims(token);
        if (parsed.isEmpty()) {
            return null;
        }
        AccessTokenClaims claims = parsed.get();
        String sessionId = claims.sessionId();
        String userId = claims.userId();
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
