package com.aionn.ucp.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ucp.profile")
public record UcpProfileProperties(
        @DefaultValue("aionn") String businessId,
        @DefaultValue("https://ucp.dev/specification/overview") String specUrl,
        @DefaultValue("/.well-known/ucp") String wellKnownPath) {
}
