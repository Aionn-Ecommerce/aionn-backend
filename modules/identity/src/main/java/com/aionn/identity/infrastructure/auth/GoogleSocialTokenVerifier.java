package com.aionn.identity.infrastructure.auth;

import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;

/**
 * Strategy interface for verifying a Google ID token. Two implementations are
 * provided: {@link MockGoogleSocialTokenVerifier} (assumes the token is valid)
 * and {@link RemoteGoogleSocialTokenVerifier} (stub for the real Google
 * tokeninfo / JWKS call which will be filled in when we lock the integration
 * down).
 */
public interface GoogleSocialTokenVerifier {

    String verifyAndExtractUserId(String providerToken);

    default void requireNotBlank(String providerToken) {
        if (providerToken == null || providerToken.isBlank()) {
            throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID);
        }
    }
}

