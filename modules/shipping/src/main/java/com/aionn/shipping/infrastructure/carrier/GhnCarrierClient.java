package com.aionn.shipping.infrastructure.carrier;

import com.aionn.shipping.application.port.out.CarrierClient;
import com.aionn.shipping.domain.valueobject.ShipmentAddress;
import com.aionn.shipping.domain.valueobject.ShipmentDimensions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(prefix = "shipping.carrier", name = "provider", havingValue = "ghn")
public class GhnCarrierClient implements CarrierClient {

    @Override
    public Quote quote(ShipmentAddress address, ShipmentDimensions dimensions, String currency) {
        // POST https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee
        throw new UnsupportedOperationException("GHN quote API is not implemented yet");
    }

    @Override
    public Registration register(String shipmentId, String orderId, ShipmentAddress address,
            ShipmentDimensions dimensions, BigDecimal codAmount, BigDecimal shippingFee, String currency) {
        // POST https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create
        throw new UnsupportedOperationException("GHN register API is not implemented yet");
    }

    @Override
    public String fetchLabel(String trackingCode) {
        // GET https://online-gateway.ghn.vn/shiip/public-api/v2/a5/gen-token
        throw new UnsupportedOperationException("GHN label API is not implemented yet");
    }

    @Override
    public void cancel(String trackingCode, String reason) {
        // POST https://online-gateway.ghn.vn/shiip/public-api/v2/switch-status/cancel
        throw new UnsupportedOperationException("GHN cancel API is not implemented yet");
    }
}

