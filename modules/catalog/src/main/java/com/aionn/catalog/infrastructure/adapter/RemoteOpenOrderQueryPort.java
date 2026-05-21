package com.aionn.catalog.infrastructure.adapter;

import com.aionn.catalog.application.port.out.OpenOrderQueryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub for the real Ordering query. Wire up once the Ordering module
 * publishes a query API or REST endpoint for "open orders by merchant".
 */
@Component
@ConditionalOnProperty(prefix = "catalog.ordering", name = "provider", havingValue = "remote")
public class RemoteOpenOrderQueryPort implements OpenOrderQueryPort {

    @Override
    public boolean hasOpenOrdersForMerchant(String merchantId) {
        throw new UnsupportedOperationException("Remote OpenOrderQueryPort is not implemented yet");
    }
}

