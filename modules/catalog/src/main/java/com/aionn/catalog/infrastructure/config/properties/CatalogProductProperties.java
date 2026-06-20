package com.aionn.catalog.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "catalog.product")
public record CatalogProductProperties(
        @DefaultValue("VND") String defaultCurrency,
        @DefaultValue(" (Copy)") String cloneNameSuffix,
        @DefaultValue("5000") int bulkMaxSize,
        @DefaultValue("200") int reindexPageSize) {
}
