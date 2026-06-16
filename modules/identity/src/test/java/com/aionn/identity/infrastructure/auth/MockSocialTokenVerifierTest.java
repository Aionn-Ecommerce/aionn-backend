package com.aionn.identity.infrastructure.auth;

import com.aionn.identity.application.port.out.social.SocialUserProfile;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.infrastructure.auth.social.facebook.FacebookSocialTokenVerifier;
import com.aionn.identity.infrastructure.auth.social.google.GoogleSocialTokenVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises the {@code requireNotBlank} default method exposed by the social
 * token verifier interfaces. Local (mock) implementations of those interfaces
 * use the same helper to reject blank provider tokens, so this verifies the
 * contract used by every concrete verifier.
 */
class MockSocialTokenVerifierTest {

    private static final GoogleSocialTokenVerifier MOCK_GOOGLE = providerToken -> {
        // Mock implementation: echo the token back as the provider user id.
        return new SocialUserProfile("google-uid-" + providerToken, "u@example.com", "User");
    };

    private static final FacebookSocialTokenVerifier MOCK_FACEBOOK = providerToken ->
            new SocialUserProfile("fb-uid-" + providerToken, null, "User");

    @Test
    void googleVerifierRejectsBlankProviderToken() {
        IdentityException ex = assertThrows(IdentityException.class,
                () -> MOCK_GOOGLE.requireNotBlank("   "));
        assertEquals(IdentityErrorCode.PROVIDER_TOKEN_INVALID.getCode(), ex.getErrorCode());
    }

    @Test
    void googleVerifierRejectsNullProviderToken() {
        assertThrows(IdentityException.class, () -> MOCK_GOOGLE.requireNotBlank(null));
    }

    @Test
    void facebookVerifierRejectsBlankProviderToken() {
        assertThrows(IdentityException.class, () -> MOCK_FACEBOOK.requireNotBlank(""));
    }

    @Test
    void requireNotBlankAcceptsRealTokens() {
        assertDoesNotThrow(() -> MOCK_GOOGLE.requireNotBlank("abc"));
        assertDoesNotThrow(() -> MOCK_FACEBOOK.requireNotBlank("xyz"));
    }

    @Test
    void mockVerifierReturnsSocialProfile() {
        SocialUserProfile profile = MOCK_GOOGLE.verify("token-123");
        assertEquals("google-uid-token-123", profile.providerUserId());
        assertEquals("u@example.com", profile.email());
    }
}
