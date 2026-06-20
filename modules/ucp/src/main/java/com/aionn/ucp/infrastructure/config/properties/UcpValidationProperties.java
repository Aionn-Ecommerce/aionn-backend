package com.aionn.ucp.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ucp.validation")
public record UcpValidationProperties(
        @DefaultValue("true") boolean strict,
        @DefaultValue("classpath:ucp/schemas/") String schemasClasspath) {
}
