package com.aionn.shipping.infrastructure.integration;

import com.aionn.shipping.application.dto.rate.result.ShippingQuoteResult;
import com.aionn.shipping.application.dto.shipment.command.CreateShipmentCommand;
import com.aionn.shipping.application.dto.shipment.command.QuoteShippingCommand;
import com.aionn.shipping.application.dto.shipment.result.ShipmentResult;
import com.aionn.shipping.application.service.ShipmentService;
import com.aionn.shipping.domain.exception.ShippingErrorCode;
import com.aionn.shipping.domain.exception.ShippingException;
import com.aionn.shipping.domain.valueobject.ShipmentAddress;
import com.aionn.shipping.domain.valueobject.ShipmentDimensions;
import com.aionn.shipping.infrastructure.config.ShippingProperties;
import com.aionn.sharedkernel.integration.port.shipping.ShippingFulfillmentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShippingFulfillmentAdapter implements ShippingFulfillmentPort {

    private final ShipmentService shipmentService;
    private final ShippingProperties properties;

    @Override
    public QuoteResult quote(String orderId, String merchantId, Address address, String currency) {
        ShippingQuoteResult result = shipmentService.quote(new QuoteShippingCommand(
                orderId, toShipmentAddress(address), defaultDimensions(), currency));
        return new QuoteResult(result.fee(), result.currency());
    }

    @Override
    public RegistrationResult createAndRegister(String orderId, String merchantId, String userId,
            Address address, java.math.BigDecimal codAmount, java.math.BigDecimal shippingFee, String currency) {
        ShipmentResult shipment = shipmentService.createAndRegister(new CreateShipmentCommand(
                orderId, merchantId, userId, toShipmentAddress(address), defaultDimensions(),
                codAmount, shippingFee, currency == null ? "VND" : currency));
        return new RegistrationResult(shipment.shipmentId(), shipment.trackingCode(),
                shipment.carrierOrderId(), shipment.labelUrl());
    }

    private ShipmentDimensions defaultDimensions() {
        ShippingProperties.DefaultDimensions d = properties.defaultDimensions();
        return new ShipmentDimensions(d.weightGram(), d.lengthCm(), d.widthCm(), d.heightCm());
    }

    private ShipmentAddress toShipmentAddress(Address address) {
        if (address == null) {
            throw new ShippingException(ShippingErrorCode.INVALID_ARGUMENT, "address is required");
        }
        return new ShipmentAddress(address.fullName(), address.phone(), address.addressLine(),
                address.wardCode(), address.districtCode(), address.provinceCode(), address.countryCode());
    }
}
