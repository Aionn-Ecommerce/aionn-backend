package com.ecommerce.identity.infrastructure.config.properties;

import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Component;

@Builder
@Component
@ConfigurationProperties(prefix = "identity.auth")
public record AuthProperties(
                @DefaultValue("X-Client-Type") String clientTypeHeader,
                @DefaultValue("mobile") String mobileClientValue) {
}
