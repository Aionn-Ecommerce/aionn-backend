package com.aionn.identity.infrastructure.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "identity.auth.social.facebook", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockFacebookSocialTokenVerifier implements FacebookSocialTokenVerifier {

    @Override
    public String verifyAndExtractUserId(String providerToken) {
        requireNotBlank(providerToken);
        return "facebook:" + Integer.toHexString(providerToken.hashCode());
    }
}

