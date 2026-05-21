package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.ShippingGateway;
import com.aionn.ordering.domain.valueobject.ShippingAddress;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "ordering.shipping", name = "provider", havingValue = "remote")
public class RemoteShippingGateway implements ShippingGateway {

    @Override
    public ShippingQuote quote(String orderId, String merchantId, ShippingAddress address, String currency) {
        throw new UnsupportedOperationException("Remote ShippingGateway is not implemented yet");
    }

    @Override
    public String createShipment(String orderId, String merchantId, ShippingAddress address) {
        throw new UnsupportedOperationException("Remote ShippingGateway is not implemented yet");
    }
}

