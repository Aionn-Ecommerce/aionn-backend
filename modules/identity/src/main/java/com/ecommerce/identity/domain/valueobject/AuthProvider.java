package com.ecommerce.identity.domain.valueobject;

import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;

public enum AuthProvider {
    GOOGLE,
    FACEBOOK;

    public static AuthProvider from(String rawProvider) {
        if (rawProvider == null || rawProvider.isBlank()) {
            throw new IdentityException(IdentityErrorCode.PROVIDER_NOT_SUPPORTED);
        }
        try {
            return AuthProvider.valueOf(rawProvider.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IdentityException(IdentityErrorCode.PROVIDER_NOT_SUPPORTED);
        }
    }
}
