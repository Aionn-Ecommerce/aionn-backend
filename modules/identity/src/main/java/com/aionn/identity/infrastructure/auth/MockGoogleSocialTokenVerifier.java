package com.aionn.identity.infrastructure.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Development-mode verifier that trusts whatever token the client sent. Use
 * for local + integration tests; production must select the real verifier via
 * {@code identity.auth.social.google.provider=remote}.
 */
@Component
@ConditionalOnProperty(prefix = "identity.auth.social.google", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockGoogleSocialTokenVerifier implements GoogleSocialTokenVerifier {

    @Override
    public String verifyAndExtractUserId(String providerToken) {
        requireNotBlank(providerToken);
        // Stable mapping for tests: hash the token to a deterministic provider id.
        return "google:" + Integer.toHexString(providerToken.hashCode());
    }
}

