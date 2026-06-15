package com.aionn.identity.infrastructure.auth.social;

import com.aionn.identity.application.port.out.social.SocialTokenVerifierPort;
import com.aionn.identity.application.port.out.social.SocialUserProfile;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.valueobject.AuthProvider;
import com.aionn.identity.infrastructure.auth.social.facebook.FacebookSocialTokenVerifier;
import com.aionn.identity.infrastructure.auth.social.google.GoogleSocialTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialTokenVerifierAdapter implements SocialTokenVerifierPort {

    private final GoogleSocialTokenVerifier googleSocialTokenVerifier;
    private final FacebookSocialTokenVerifier facebookSocialTokenVerifier;

    @Override
    public SocialUserProfile verifyAndExtract(AuthProvider provider, String providerToken) {
        return switch (provider) {
            case GOOGLE -> googleSocialTokenVerifier.verify(providerToken);
            case FACEBOOK -> facebookSocialTokenVerifier.verify(providerToken);
            default -> throw new IdentityException(IdentityErrorCode.PROVIDER_NOT_SUPPORTED);
        };
    }
}
