package com.aionn.inventory.infrastructure.integration;

import com.aionn.inventory.application.port.out.SafetyStockNotifier;
import com.aionn.sharedkernel.integration.event.inventory.SafetyStockBreachedIntegrationEvent;
import com.aionn.sharedkernel.integration.publisher.IntegrationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class InventorySafetyStockNotifier implements SafetyStockNotifier {

    private final IntegrationEventPublisher integrationEventPublisher;

    @Override
    public void notifySafetyStockBreach(
            String merchantId, String skuId, String warehouseId, int availableQty, int safetyStockQty) {
        integrationEventPublisher.publish(new SafetyStockBreachedIntegrationEvent(
                null, Instant.now(), merchantId, skuId, warehouseId, availableQty, safetyStockQty));
    }
}
