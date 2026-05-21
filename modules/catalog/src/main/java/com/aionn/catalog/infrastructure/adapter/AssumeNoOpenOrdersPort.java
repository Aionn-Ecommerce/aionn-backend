package com.aionn.catalog.infrastructure.adapter;

import com.aionn.catalog.application.port.out.OpenOrderQueryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default OpenOrderQueryPort. Until the Ordering bounded context exposes a
 * real query API we assume there are no open orders. This is safe for the
 * monolith dev/test environment where Ordering is not implemented yet.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "catalog.ordering", name = "provider", havingValue = "assume-empty", matchIfMissing = true)
public class AssumeNoOpenOrdersPort implements OpenOrderQueryPort {

    @Override
    public boolean hasOpenOrdersForMerchant(String merchantId) {
        log.debug("Assume-no-open-orders: returning false for {}", merchantId);
        return false;
    }
}

