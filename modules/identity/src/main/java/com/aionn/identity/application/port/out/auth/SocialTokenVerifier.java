package com.aionn.identity.application.port.out.auth;

import com.aionn.identity.domain.valueobject.AuthProvider;

public interface SocialTokenVerifier {
	String verifyAndExtractProviderUserId(AuthProvider provider, String providerToken);
}



