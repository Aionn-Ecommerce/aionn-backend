package com.ecommerce.identity.infrastructure.config.properties;

import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for agent identity management.
 */
@Builder
@Component
@ConfigurationProperties(prefix = "identity.agent")
public record AgentProperties(
        @DefaultValue("1") int keyExpiryYears) {
}
