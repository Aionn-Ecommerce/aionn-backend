package com.aionn.shipping.infrastructure.integration;

import com.aionn.shipping.application.dto.rate.result.ShippingQuoteResult;
import com.aionn.shipping.application.dto.shipment.command.ShipmentCommands;
import com.aionn.shipping.application.dto.shipment.result.ShipmentResult;
import com.aionn.shipping.application.service.ShipmentService;
import com.aionn.shipping.domain.valueobject.ShipmentAddress;
import com.aionn.shipping.domain.valueobject.ShipmentDimensions;
import com.aionn.sharedkernel.integration.port.shipping.ShippingFulfillmentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ShippingFulfillmentAdapter implements ShippingFulfillmentPort {

    /**
     * Default parcel dimensions used when the caller has no item-level data yet.
     */
    private static final ShipmentDimensions DEFAULT_DIMENSIONS = new ShipmentDimensions(
            500, new BigDecimal("20"), new BigDecimal("15"), new BigDecimal("10"));

    private final ShipmentService shipmentService;

    @Override
    public QuoteResult quote(String orderId, String merchantId, Address address, String currency) {
        ShippingQuoteResult result = shipmentService.quote(new ShipmentCommands.QuoteShipping(
                orderId, toShipmentAddress(address), DEFAULT_DIMENSIONS, currency));
        return new QuoteResult(result.fee(), result.currency());
    }

    @Override
    public String createShipment(String orderId, String merchantId, Address address) {
        ShipmentResult shipment = shipmentService.createShipment(new ShipmentCommands.CreateShipment(
                orderId, toShipmentAddress(address), DEFAULT_DIMENSIONS, null, null, "VND"));
        return shipment.shipmentId();
    }

    private ShipmentAddress toShipmentAddress(Address address) {
        if (address == null) {
            return null;
        }
        return new ShipmentAddress(address.fullName(), address.phone(), address.addressLine(),
                address.wardCode(), address.districtCode(), address.provinceCode(), address.countryCode());
    }
}
