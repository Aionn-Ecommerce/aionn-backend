package com.aionn.identity.application.port.out.social;

import com.aionn.identity.domain.valueobject.AuthProvider;

public interface SocialTokenVerifierPort {
	String verifyAndExtractProviderUserId(AuthProvider provider, String providerToken);
}
