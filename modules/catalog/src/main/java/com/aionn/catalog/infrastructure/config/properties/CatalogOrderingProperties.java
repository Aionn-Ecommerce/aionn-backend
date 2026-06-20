package com.aionn.catalog.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "catalog.ordering")
public record CatalogOrderingProperties(
        @DefaultValue("assume-empty") String provider) {
}
