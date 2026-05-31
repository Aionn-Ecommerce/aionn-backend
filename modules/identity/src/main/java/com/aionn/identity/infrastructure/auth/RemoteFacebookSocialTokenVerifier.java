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

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "identity.auth.social.facebook", name = "provider", havingValue = "remote")
public class RemoteFacebookSocialTokenVerifier implements FacebookSocialTokenVerifier {

    private final SocialAuthProperties socialAuthProperties;

    @Override
    public String verifyAndExtractUserId(String providerToken) {
        requireNotBlank(providerToken);

        SocialAuthProperties.Facebook config = socialAuthProperties.facebook();
        if (config == null
                || config.appId() == null || config.appId().isBlank()
                || config.appSecret() == null || config.appSecret().isBlank()) {
            throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                    "Facebook app credentials are not configured");
        }

        RestClient client = RestClient.builder()
                .baseUrl(config.debugTokenUrl())
                .build();

        try {
            String appAccessToken = config.appId() + "|" + config.appSecret();
            FacebookDebugTokenResponse response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("input_token", providerToken)
                            .queryParam("access_token", appAccessToken)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(FacebookDebugTokenResponse.class);

            FacebookDebugTokenData data = response == null ? null : response.data();
            if (data == null) {
                throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                        "Facebook debug_token payload is malformed");
            }

            if (!Boolean.TRUE.equals(data.is_valid())) {
                throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                        "Facebook token is invalid");
            }
            if (!config.appId().equals(data.app_id())) {
                throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                        "Facebook app id mismatch");
            }
            if (data.user_id() == null || data.user_id().isBlank()) {
                throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                        "Facebook token user id is missing");
            }

            return data.user_id();
        } catch (IdentityException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to verify Facebook token", ex);
            throw new IdentityException(IdentityErrorCode.PROVIDER_TOKEN_INVALID,
                    "Facebook token verification failed");
        }
    }

    private record FacebookDebugTokenResponse(FacebookDebugTokenData data) {
    }

    private record FacebookDebugTokenData(Boolean is_valid, String app_id, String user_id) {
    }
}
