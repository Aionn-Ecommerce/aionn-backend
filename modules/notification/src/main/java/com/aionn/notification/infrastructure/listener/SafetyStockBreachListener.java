package com.aionn.notification.infrastructure.listener;

import com.aionn.notification.application.dto.notification.command.NotificationCommands;
import com.aionn.notification.application.service.NotificationDispatchService;
import com.aionn.notification.domain.valueobject.NotificationCategory;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import com.aionn.sharedkernel.integration.event.inventory.SafetyStockBreachedIntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Notifies merchants when one of their SKUs drops to or below the configured
 * safety-stock threshold. Best-effort, non-blocking.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SafetyStockBreachListener {

    private static final String EVENT_TYPE = "INVENTORY_SAFETY_STOCK_BREACH";
    private static final List<NotificationChannel> CHANNELS = List.of(
            NotificationChannel.EMAIL, NotificationChannel.IN_APP, NotificationChannel.PUSH);

    private final NotificationDispatchService notificationDispatchService;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSafetyStockBreached(SafetyStockBreachedIntegrationEvent event) {
        try {
            notificationDispatchService.sendByEvent(new NotificationCommands.SendByEvent(
                    event.merchantId(),
                    EVENT_TYPE,
                    NotificationCategory.SYSTEM,
                    CHANNELS,
                    null,
                    null,
                    Map.of(
                            "skuId", event.skuId(),
                            "warehouseId", event.warehouseId(),
                            "availableQty", String.valueOf(event.availableQty()),
                            "safetyStockQty", String.valueOf(event.safetyStockQty()))));
        } catch (RuntimeException ex) {
            log.error("Failed to dispatch safety-stock notification for merchant {}", event.merchantId(), ex);
        }
    }
}
