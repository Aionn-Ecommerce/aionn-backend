package com.aionn.catalog.infrastructure.gateway;

import com.aionn.sharedkernel.integration.port.catalog.OpenOrderQueryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "catalog.ordering", name = "provider", havingValue = "assume-empty", matchIfMissing = true)
public class AssumeNoOpenOrdersAdapter implements OpenOrderQueryPort {

    @Override
    public boolean hasOpenOrdersForMerchant(String merchantId) {
        log.debug("Assume-no-open-orders: returning false for {}", merchantId);
        return false;
    }
}
