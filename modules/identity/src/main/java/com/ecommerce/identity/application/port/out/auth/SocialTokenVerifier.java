package com.ecommerce.identity.application.port.out.auth;

import com.ecommerce.identity.domain.valueobject.AuthProvider;

public interface SocialTokenVerifier {
	String verifyAndExtractProviderUserId(AuthProvider provider, String providerToken);
}


