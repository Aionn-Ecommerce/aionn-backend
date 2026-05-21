package com.aionn.identity.infrastructure.auth;

import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub for the real Facebook Graph API token verification call. Wire up by
 * calling {@code https://graph.facebook.com/debug_token} with an app access
 * token and validating {@code app_id}, {@code is_valid} and {@code user_id}.
 */
@Component
@ConditionalOnProperty(prefix = "identity.auth.social.facebook", name = "provider", havingValue = "remote")
public class RemoteFacebookSocialTokenVerifier implements FacebookSocialTokenVerifier {

    @Override
    public String verifyAndExtractUserId(String providerToken) {
        requireNotBlank(providerToken);
        throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                "Remote Facebook verifier is not implemented yet");
    }
}

