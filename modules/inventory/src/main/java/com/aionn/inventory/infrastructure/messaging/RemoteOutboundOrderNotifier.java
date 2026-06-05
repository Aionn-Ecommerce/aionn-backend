package com.aionn.inventory.infrastructure.messaging;

import com.aionn.inventory.application.port.out.OutboundOrderNotifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "inventory.outbound-notifier", name = "provider", havingValue = "remote")
public class RemoteOutboundOrderNotifier implements OutboundOrderNotifier {

    @Override
    public void notifyOutbound(String orderId, String skuId, String warehouseId, int qty) {
        throw new UnsupportedOperationException("Remote OutboundOrderNotifier is not implemented yet");
    }

    @Override
    public void notifyReservationFailed(String orderId, String skuId, String warehouseId, int qty, String reason) {
        throw new UnsupportedOperationException("Remote OutboundOrderNotifier is not implemented yet");
    }
}
