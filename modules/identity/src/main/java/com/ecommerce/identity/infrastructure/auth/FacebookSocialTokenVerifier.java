package com.ecommerce.identity.infrastructure.auth;

import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import org.springframework.stereotype.Component;

@Component
public class FacebookSocialTokenVerifier {

    public String verifyAndExtractUserId(String providerToken) {
        if (providerToken == null || providerToken.isBlank()) {
            throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID);
        }
        return "facebook:" + Integer.toHexString(providerToken.hashCode());
    }
}
