package com.aionn.identity.infrastructure.auth.social;

import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;

public interface FacebookSocialTokenVerifier {

    String verifyAndExtractUserId(String providerToken);

    default void requireNotBlank(String providerToken) {
        if (providerToken == null || providerToken.isBlank()) {
            throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID);
        }
    }
}
