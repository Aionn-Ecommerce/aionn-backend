package com.aionn.identity.infrastructure.config.properties;

import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Builder
@ConfigurationProperties(prefix = "identity.auth")
public record AuthProperties(
        @DefaultValue("X-Client-Type") String clientTypeHeader,
        @DefaultValue("mobile") String mobileClientValue) {
}

