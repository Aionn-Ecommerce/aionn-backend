package com.aionn.identity.infrastructure.auth.social;

import com.aionn.identity.domain.valueobject.AuthProvider;
import com.aionn.identity.infrastructure.auth.social.FacebookSocialTokenVerifier;

import com.aionn.identity.infrastructure.auth.social.GoogleSocialTokenVerifier;

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
        when(googleVerifier.verifyAndExtractUserId("token")).thenReturn("google-user-1");

        String result = adapter.verifyAndExtractProviderUserId(AuthProvider.GOOGLE, "token");

        assertEquals("google-user-1", result);
        verify(facebookVerifier, never()).verifyAndExtractUserId(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void facebookProviderDelegatesToFacebookVerifier() {
        when(facebookVerifier.verifyAndExtractUserId("token")).thenReturn("fb-user-1");

        String result = adapter.verifyAndExtractProviderUserId(AuthProvider.FACEBOOK, "token");

        assertEquals("fb-user-1", result);
        verify(googleVerifier, never()).verifyAndExtractUserId(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void googleAndFacebookProvidersDispatchIndependently() {
        when(googleVerifier.verifyAndExtractUserId("g-token")).thenReturn("g-id");
        when(facebookVerifier.verifyAndExtractUserId("f-token")).thenReturn("f-id");

        assertEquals("g-id", adapter.verifyAndExtractProviderUserId(AuthProvider.GOOGLE, "g-token"));
        assertEquals("f-id", adapter.verifyAndExtractProviderUserId(AuthProvider.FACEBOOK, "f-token"));
    }
}
