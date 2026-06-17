package com.aionn.catalog.infrastructure.config;

import com.aionn.catalog.infrastructure.config.properties.CatalogOrderingProperties;
import com.aionn.catalog.infrastructure.config.properties.CatalogSearchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogProviderConfigurationValidator implements SmartInitializingSingleton {

    private final CatalogSearchProperties searchProperties;
    private final CatalogOrderingProperties orderingProperties;

    @Override
    public void afterSingletonsInstantiated() {
        validateOpenSearch();
        validateOrderingProvider();
    }

    private void validateOpenSearch() {
        if (!"opensearch".equalsIgnoreCase(searchProperties.provider())) {
            return;
        }
        CatalogSearchProperties.OpenSearch cfg = searchProperties.opensearch();
        if (cfg == null) {
            throw new IllegalStateException(
                    "catalog.search.opensearch configuration is required when provider=opensearch");
        }
        requireNotBlank(cfg.host(), "CATALOG_SEARCH_OPENSEARCH_HOST");
        requireNotBlank(cfg.indexName(), "CATALOG_SEARCH_OPENSEARCH_INDEX");
    }

    private void validateOrderingProvider() {
        String provider = orderingProperties.provider();
        if (!"assume-empty".equalsIgnoreCase(provider)) {
            return;
        }
        log.warn("catalog.ordering.provider={} - merchant.close() will not check open orders."
                + " Switch to a real provider before going live.", provider);
    }

    private void requireNotBlank(String value, String envName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration: " + envName);
        }
    }
}
