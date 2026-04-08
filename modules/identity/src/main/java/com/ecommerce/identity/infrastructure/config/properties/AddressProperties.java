package com.ecommerce.identity.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "identity.address")
public record AddressProperties(
        long maxAddressNumbers) {
}
