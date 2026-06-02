package com.aionn.identity.infrastructure.auth.social;

import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.infrastructure.auth.social.MockFacebookSocialTokenVerifier;

import com.aionn.identity.infrastructure.auth.social.MockGoogleSocialTokenVerifier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockSocialTokenVerifierTest {

    private final MockGoogleSocialTokenVerifier googleVerifier = new MockGoogleSocialTokenVerifier();
    private final MockFacebookSocialTokenVerifier facebookVerifier = new MockFacebookSocialTokenVerifier();

    @Test
    void googleVerifierReturnsDeterministicIdForSameToken() {
        String id1 = googleVerifier.verifyAndExtractUserId("any-token");
        String id2 = googleVerifier.verifyAndExtractUserId("any-token");

        assertEquals(id1, id2);
        assertTrue(id1.startsWith("mock-google-"));
    }

    @Test
    void googleVerifierReturnsDifferentIdsForDifferentTokens() {
        assertNotEquals(
                googleVerifier.verifyAndExtractUserId("token-a"),
                googleVerifier.verifyAndExtractUserId("token-b"));
    }

    @Test
    void googleVerifierRejectsBlankToken() {
        var ex = assertThrows(IdentityException.class,
                () -> googleVerifier.verifyAndExtractUserId("  "));

        assertEquals(IdentityErrorCode.PROVIDER_TOKEN_INVALID.getCode(), ex.getErrorCode());
    }

    @Test
    void facebookVerifierReturnsDeterministicIdForSameToken() {
        String id1 = facebookVerifier.verifyAndExtractUserId("any-token");
        String id2 = facebookVerifier.verifyAndExtractUserId("any-token");

        assertEquals(id1, id2);
        assertTrue(id1.startsWith("mock-facebook-"));
    }

    @Test
    void facebookVerifierRejectsNullToken() {
        var ex = assertThrows(IdentityException.class,
                () -> facebookVerifier.verifyAndExtractUserId(null));

        assertEquals(IdentityErrorCode.PROVIDER_TOKEN_INVALID.getCode(), ex.getErrorCode());
    }
}
