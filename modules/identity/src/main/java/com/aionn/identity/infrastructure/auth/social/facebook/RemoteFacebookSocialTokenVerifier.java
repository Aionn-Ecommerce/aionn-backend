package com.aionn.identity.infrastructure.auth.social.facebook;

import com.aionn.identity.application.port.out.social.SocialUserProfile;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.infrastructure.config.properties.SocialAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
public class RemoteFacebookSocialTokenVerifier implements FacebookSocialTokenVerifier {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);
    private static final String GRAPH_ME_URL = "https://graph.facebook.com/me";

    private final SocialAuthProperties socialAuthProperties;
    private final RestClient debugRestClient;
    private final RestClient graphRestClient;

    public RemoteFacebookSocialTokenVerifier(SocialAuthProperties socialAuthProperties) {
        this.socialAuthProperties = socialAuthProperties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) CONNECT_TIMEOUT.toMillis());
        factory.setReadTimeout((int) READ_TIMEOUT.toMillis());
        SocialAuthProperties.Facebook config = socialAuthProperties.facebook();
        String debugUrl = config != null ? config.debugTokenUrl() : null;
        RestClient.Builder debugBuilder = RestClient.builder().requestFactory(factory);
        if (debugUrl != null && !debugUrl.isBlank()) {
            debugBuilder.baseUrl(debugUrl);
        }
        this.debugRestClient = debugBuilder.build();
        this.graphRestClient = RestClient.builder().requestFactory(factory).baseUrl(GRAPH_ME_URL).build();
    }

    @Override
    public SocialUserProfile verify(String providerToken) {
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
            FacebookDebugTokenResponse response = debugRestClient.get()
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

            FacebookProfileResponse profile = null;
            try {
                profile = graphRestClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("fields", "id,name,email")
                                .queryParam("access_token", providerToken)
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(FacebookProfileResponse.class);
            } catch (Exception ex) {
                log.warn("Failed to fetch Facebook /me profile (continuing without it)", ex);
            }

            String email = profile != null ? profile.email() : null;
            String displayName = profile != null ? profile.name() : null;
            return new SocialUserProfile(data.user_id(), email, displayName);
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

    private record FacebookProfileResponse(String id, String name, String email) {
    }
}
