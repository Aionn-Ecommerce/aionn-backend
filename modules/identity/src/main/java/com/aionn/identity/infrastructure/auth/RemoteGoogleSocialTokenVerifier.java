package com.aionn.identity.infrastructure.auth;

import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Real-mode verifier that will call Google's tokeninfo / JWKS endpoint. The
 * integration is intentionally left as a stub: we have not yet decided which
 * Google library to use, and an unfinished implementation here would silently
 * accept any token. We fail closed instead.
 *
 * <p>
 * To wire up the real implementation, add a Google API client (or
 * {@code google-api-client} / {@code google-oauth-client}), validate the ID
 * token signature against {@code https://www.googleapis.com/oauth2/v3/certs},
 * and verify the {@code aud} claim matches the configured Google client id.
 */
@Component
@ConditionalOnProperty(prefix = "identity.auth.social.google", name = "provider", havingValue = "remote")
public class RemoteGoogleSocialTokenVerifier implements GoogleSocialTokenVerifier {

    @Override
    public String verifyAndExtractUserId(String providerToken) {
        requireNotBlank(providerToken);
        throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                "Remote Google verifier is not implemented yet");
    }
}

