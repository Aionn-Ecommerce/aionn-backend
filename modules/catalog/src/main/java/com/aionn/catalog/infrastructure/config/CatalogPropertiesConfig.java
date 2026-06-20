package com.aionn.catalog.infrastructure.config;

import com.aionn.catalog.infrastructure.config.properties.CatalogOrderingProperties;
import com.aionn.catalog.infrastructure.config.properties.CatalogProductProperties;
import com.aionn.catalog.infrastructure.config.properties.CatalogSearchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        CatalogOrderingProperties.class,
        CatalogProductProperties.class,
        CatalogSearchProperties.class
})
public class CatalogPropertiesConfig {
}
