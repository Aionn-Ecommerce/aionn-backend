package com.aionn.ucp.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ucp.signing")
public record UcpSigningProperties(
        @DefaultValue("aionn-ucp-key-1") String keyId,
        @DefaultValue("ES256") String algorithm,
        @DefaultValue("P-256") String curve,
        @DefaultValue("") String privateKeyPem,
        @DefaultValue("") String publicKeyPem) {
}
