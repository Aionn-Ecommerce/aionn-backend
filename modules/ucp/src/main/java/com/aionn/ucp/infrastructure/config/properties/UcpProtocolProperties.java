package com.aionn.ucp.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ucp.protocol")
public record UcpProtocolProperties(
        @DefaultValue("2025-01-01") String version,
        @DefaultValue("https://ucp.dev/schemas") String schemaBaseUrl) {
}
