package com.aionn.identity.infrastructure.auth.social;

import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.infrastructure.auth.social.FacebookSocialTokenVerifier;
import com.aionn.identity.infrastructure.config.properties.SocialAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "identity.auth.social.facebook", name = "provider", havingValue = "remote")
public class RemoteFacebookSocialTokenVerifier implements FacebookSocialTokenVerifier {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final SocialAuthProperties socialAuthProperties;
    private final RestClient restClient;

    public RemoteFacebookSocialTokenVerifier(SocialAuthProperties socialAuthProperties) {
        this.socialAuthProperties = socialAuthProperties;
        // Single client + explicit timeouts: see RemoteGoogleSocialTokenVerifier for
        // the
        // rationale (avoid per-call allocation and prevent upstream stalls from
        // blocking
        // request worker threads indefinitely).
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) CONNECT_TIMEOUT.toMillis());
        factory.setReadTimeout((int) READ_TIMEOUT.toMillis());
        SocialAuthProperties.Facebook config = socialAuthProperties.facebook();
        String baseUrl = config != null ? config.debugTokenUrl() : null;
        RestClient.Builder builder = RestClient.builder().requestFactory(factory);
        if (baseUrl != null && !baseUrl.isBlank()) {
            builder.baseUrl(baseUrl);
        }
        this.restClient = builder.build();
    }

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

        try {
            String appAccessToken = config.appId() + "|" + config.appSecret();
            FacebookDebugTokenResponse response = restClient.get()
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
