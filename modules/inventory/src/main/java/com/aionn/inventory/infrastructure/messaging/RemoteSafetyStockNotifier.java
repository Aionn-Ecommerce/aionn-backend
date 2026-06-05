package com.aionn.inventory.infrastructure.messaging;

import com.aionn.inventory.application.port.out.SafetyStockNotifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub for the future Notification module integration. Fails closed so
 * misconfigured environments do not silently drop alerts.
 */
@Component
@ConditionalOnProperty(prefix = "inventory.safety-stock", name = "provider", havingValue = "remote")
public class RemoteSafetyStockNotifier implements SafetyStockNotifier {

    @Override
    public void notifySafetyStockBreach(
            String merchantId, String skuId, String warehouseId, int availableQty, int safetyStockQty) {
        throw new UnsupportedOperationException("Remote SafetyStockNotifier is not implemented yet");
    }
}
