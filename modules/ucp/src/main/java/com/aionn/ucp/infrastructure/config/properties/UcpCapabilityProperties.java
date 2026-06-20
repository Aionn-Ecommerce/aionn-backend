package com.aionn.ucp.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ucp.capabilities")
public record UcpCapabilityProperties(
        @DefaultValue Capability cart,
        @DefaultValue Capability checkout,
        @DefaultValue Capability order,
        @DefaultValue Capability identityLinking) {

    public record Capability(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("2025-01-01") String version) {
    }
}
