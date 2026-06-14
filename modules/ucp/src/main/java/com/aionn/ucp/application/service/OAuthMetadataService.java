package com.aionn.ucp.application.service;

import com.aionn.ucp.infrastructure.config.UcpProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthMetadataService {

    private final UcpProperties properties;

    public Map<String, Object> buildMetadata() {
        String baseUrl = properties.getEndpointBaseUrl();
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("issuer", baseUrl);
        metadata.put("authorization_endpoint", baseUrl + "/oauth2/authorize");
        metadata.put("token_endpoint", baseUrl + "/oauth2/token");
        metadata.put("revocation_endpoint", baseUrl + "/oauth2/revoke");
        metadata.put("jwks_uri", baseUrl + "/oauth2/jwks");

        // UCP-required scopes
        metadata.put("scopes_supported", List.of(
                "dev.ucp.shopping.checkout:manage",
                "dev.ucp.shopping.order:read",
                "dev.ucp.shopping.order:manage"));

        metadata.put("response_types_supported", List.of("code"));
        metadata.put("grant_types_supported", List.of("authorization_code", "refresh_token"));
        metadata.put("code_challenge_methods_supported", List.of("S256"));

        metadata.put("token_endpoint_auth_methods_supported", List.of(
                "client_secret_basic", "none"));

        metadata.put("authorization_response_iss_parameter_supported", true);

        metadata.put("service_documentation", baseUrl + "/docs/oauth2");

        return metadata;
    }
}
