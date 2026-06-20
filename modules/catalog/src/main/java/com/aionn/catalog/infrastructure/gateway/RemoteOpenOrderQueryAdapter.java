package com.aionn.catalog.infrastructure.gateway;

import com.aionn.sharedkernel.integration.port.catalog.OpenOrderQueryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "catalog.ordering", name = "provider", havingValue = "remote")
public class RemoteOpenOrderQueryAdapter implements OpenOrderQueryPort {

    @Override
    public boolean hasOpenOrdersForMerchant(String merchantId) {
        throw new UnsupportedOperationException("Remote OpenOrderQueryPort is not implemented yet");
    }
}
