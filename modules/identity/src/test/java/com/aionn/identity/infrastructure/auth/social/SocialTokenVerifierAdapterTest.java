package com.aionn.identity.infrastructure.auth.social;

import com.aionn.identity.application.port.out.social.SocialUserProfile;
import com.aionn.identity.domain.valueobject.AuthProvider;
import com.aionn.identity.infrastructure.auth.social.facebook.FacebookSocialTokenVerifier;
import com.aionn.identity.infrastructure.auth.social.google.GoogleSocialTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialTokenVerifierAdapterTest {

    @Mock
    private GoogleSocialTokenVerifier googleVerifier;
    @Mock
    private FacebookSocialTokenVerifier facebookVerifier;

    private SocialTokenVerifierAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SocialTokenVerifierAdapter(googleVerifier, facebookVerifier);
    }

    @Test
    void googleProviderDelegatesToGoogleVerifier() {
        SocialUserProfile profile = new SocialUserProfile("google-user-1", "u@example.com", "Test User");
        when(googleVerifier.verify("token")).thenReturn(profile);

        SocialUserProfile result = adapter.verifyAndExtract(AuthProvider.GOOGLE, "token");

        assertEquals("google-user-1", result.providerUserId());
        verify(facebookVerifier, never()).verify(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void facebookProviderDelegatesToFacebookVerifier() {
        SocialUserProfile profile = new SocialUserProfile("fb-user-1", null, "FB User");
        when(facebookVerifier.verify("token")).thenReturn(profile);

        SocialUserProfile result = adapter.verifyAndExtract(AuthProvider.FACEBOOK, "token");

        assertEquals("fb-user-1", result.providerUserId());
        verify(googleVerifier, never()).verify(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void googleAndFacebookProvidersDispatchIndependently() {
        when(googleVerifier.verify("g-token")).thenReturn(new SocialUserProfile("g-id", null, null));
        when(facebookVerifier.verify("f-token")).thenReturn(new SocialUserProfile("f-id", null, null));

        assertEquals("g-id", adapter.verifyAndExtract(AuthProvider.GOOGLE, "g-token").providerUserId());
        assertEquals("f-id", adapter.verifyAndExtract(AuthProvider.FACEBOOK, "f-token").providerUserId());
    }
}
