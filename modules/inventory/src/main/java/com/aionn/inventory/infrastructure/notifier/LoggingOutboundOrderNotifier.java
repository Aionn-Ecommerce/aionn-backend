package com.aionn.inventory.infrastructure.notifier;

import com.aionn.inventory.application.port.out.OutboundOrderNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "inventory.outbound-notifier", name = "provider", havingValue = "logging", matchIfMissing = true)
public class LoggingOutboundOrderNotifier implements OutboundOrderNotifier {

    @Override
    public void notifyOutbound(String orderId, String skuId, String warehouseId, int qty) {
        log.info("[OUTBOUND] order={} sku={} warehouse={} qty={}", orderId, skuId, warehouseId, qty);
    }

    @Override
    public void notifyReservationFailed(String orderId, String skuId, String warehouseId, int qty, String reason) {
        log.warn("[RESERVATION-FAILED] order={} sku={} warehouse={} qty={} reason={}",
                orderId, skuId, warehouseId, qty, reason);
    }
}

