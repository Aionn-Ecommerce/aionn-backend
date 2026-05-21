package com.aionn.ordering.application.port.out;

import com.aionn.ordering.domain.valueobject.ShippingAddress;

import java.math.BigDecimal;

/**
 * Outbound port for shipping. Used to quote shipping fees on shipping-info
 * change and to dispatch a shipment when an order moves to PREPARING. Same
 * 2-impl pattern as the other external integrations.
 */
public interface ShippingGateway {

    ShippingQuote quote(String orderId, String merchantId, ShippingAddress address, String currency);

    String createShipment(String orderId, String merchantId, ShippingAddress address);

    record ShippingQuote(BigDecimal fee, String currency) {
    }
}

