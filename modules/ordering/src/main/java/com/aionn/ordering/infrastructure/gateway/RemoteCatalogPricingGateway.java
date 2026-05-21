package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.CatalogPricingGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "ordering.catalog-pricing", name = "provider", havingValue = "remote")
public class RemoteCatalogPricingGateway implements CatalogPricingGateway {

    @Override
    public Map<String, SkuPricing> resolve(List<String> skuIds) {
        throw new UnsupportedOperationException("Remote CatalogPricingGateway is not implemented yet");
    }
}

