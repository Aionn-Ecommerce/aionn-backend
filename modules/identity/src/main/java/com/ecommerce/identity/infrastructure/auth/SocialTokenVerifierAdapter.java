package com.ecommerce.identity.infrastructure.auth;

import com.ecommerce.identity.application.port.out.auth.SocialTokenVerifier;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.valueobject.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialTokenVerifierAdapter implements SocialTokenVerifier {

    private final GoogleSocialTokenVerifier googleSocialTokenVerifier;
    private final FacebookSocialTokenVerifier facebookSocialTokenVerifier;

    @Override
    public String verifyAndExtractProviderUserId(AuthProvider provider, String providerToken) {
        return switch (provider) {
            case GOOGLE -> googleSocialTokenVerifier.verifyAndExtractUserId(providerToken);
            case FACEBOOK -> facebookSocialTokenVerifier.verifyAndExtractUserId(providerToken);
            default -> throw new IdentityException(IdentityErrorCode.PROVIDER_NOT_SUPPORTED);
        };
    }
}


