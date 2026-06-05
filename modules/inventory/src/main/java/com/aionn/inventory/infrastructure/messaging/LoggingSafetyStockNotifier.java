package com.aionn.inventory.infrastructure.messaging;

import com.aionn.inventory.application.port.out.SafetyStockNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default implementation that logs the breach. Use this in dev/test or when
 * the merchant notification channel is not yet wired in.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "inventory.safety-stock", name = "provider", havingValue = "logging", matchIfMissing = true)
public class LoggingSafetyStockNotifier implements SafetyStockNotifier {

    @Override
    public void notifySafetyStockBreach(
            String merchantId, String skuId, String warehouseId, int availableQty, int safetyStockQty) {
        log.warn("[SAFETY-STOCK] merchant={} sku={} warehouse={} available={} threshold={}",
                merchantId, skuId, warehouseId, availableQty, safetyStockQty);
    }
}
