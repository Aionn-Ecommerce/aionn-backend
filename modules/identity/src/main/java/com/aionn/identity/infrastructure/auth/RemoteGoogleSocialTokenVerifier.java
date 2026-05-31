package com.aionn.identity.infrastructure.auth;

import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.infrastructure.config.properties.SocialAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "identity.auth.social.google", name = "provider", havingValue = "remote")
public class RemoteGoogleSocialTokenVerifier implements GoogleSocialTokenVerifier {

    private static final Set<String> VALID_ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final SocialAuthProperties socialAuthProperties;

    @Override
    public String verifyAndExtractUserId(String providerToken) {
        requireNotBlank(providerToken);

        SocialAuthProperties.Google config = socialAuthProperties.google();
        if (config == null || config.clientId() == null || config.clientId().isBlank()) {
            throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                    "Google client id is not configured");
        }

        RestClient client = RestClient.builder()
                .baseUrl(config.tokenInfoUrl())
                .build();

        try {
            GoogleTokenInfoResponse response = client.get()
                    .uri(uriBuilder -> uriBuilder.queryParam("id_token", providerToken).build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(GoogleTokenInfoResponse.class);

            if (response == null) {
                throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                        "Google tokeninfo returned no payload");
            }

            if (!config.clientId().equals(response.aud())) {
                throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                        "Google token audience mismatch");
            }
            if (!VALID_ISSUERS.contains(response.iss())) {
                throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                        "Google token issuer mismatch");
            }
            if (response.sub() == null || response.sub().isBlank()) {
                throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                        "Google token subject is missing");
            }
            if (response.exp() != null && response.exp() <= Instant.now().getEpochSecond()) {
                throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                        "Google token has expired");
            }

            return response.sub();
        } catch (IdentityException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to verify Google token", ex);
            throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                    "Google token verification failed");
        }
    }

    private record GoogleTokenInfoResponse(String aud, String iss, String sub, Long exp) {
    }
}
