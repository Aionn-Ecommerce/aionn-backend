package com.aionn.ucp.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ucp")
public class UcpProperties {

    private String protocolVersion;

    private String schemaBaseUrl;

    private String specBaseUrl;

    private String endpointBaseUrl;

    private final Signature signature = new Signature();
    private final Capabilities capabilities = new Capabilities();
    private final PlatformProfile platformProfile = new PlatformProfile();
    private final Webhook webhook = new Webhook();

    @Data
    public static class Signature {
        private boolean enforce;
        private String keyId;
        private String privateKeyPem;
        private String publicKeyPem;
    }

    @Data
    public static class Capabilities {
        private boolean catalogSearch = true;
        private boolean catalogLookup = true;
        private boolean cart = true;
        private boolean checkout = true;
        private boolean order = true;
        private boolean fulfillment = true;
        private boolean discount = true;
        private boolean identityLinking = true;
    }

    @Data
    public static class PlatformProfile {
        private long cacheTtlSeconds = 600;
        private long fetchTimeoutMs = 5000;
    }

    @Data
    public static class Webhook {
        private boolean enabled = true;
        private long delayMs = 30000;
        private int batchSize = 50;
        private int maxAttempts = 5;
        private long requestTimeoutMs = 5000;
    }
}
