package com.aionn.chat.infrastructure.realtime;

import com.aionn.sharedkernel.integration.port.identity.AccessTokenVerifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the principal for an incoming STOMP CONNECT frame by delegating
 * token verification to identity through {@link AccessTokenVerifierPort}.
 *
 * <p>Chat does not own user sessions — it must not read the session repository
 * directly. The shared-kernel port is implemented inside identity (modular
 * monolith) or by an HTTP/gRPC adapter (microservices); chat treats the
 * resolution as a single black-box call.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompPrincipalResolver {

    private final AccessTokenVerifierPort accessTokenVerifier;

    public String resolveUserId(String authorizationHeader) {
        return accessTokenVerifier.verifyAndExtractUserId(authorizationHeader).orElse(null);
    }
}
