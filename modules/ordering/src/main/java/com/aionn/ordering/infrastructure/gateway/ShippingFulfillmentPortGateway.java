package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.ShippingGateway;
import com.aionn.ordering.domain.valueobject.ShippingAddress;
import com.aionn.sharedkernel.integration.port.shipping.ShippingFulfillmentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShippingFulfillmentPortGateway implements ShippingGateway {

    private final ShippingFulfillmentPort shippingFulfillmentPort;

    @Override
    public ShippingQuote quote(String orderId, String merchantId, ShippingAddress address, String currency) {
        ShippingFulfillmentPort.QuoteResult result = shippingFulfillmentPort.quote(
                orderId, merchantId, toPortAddress(address), currency);
        return new ShippingQuote(result.fee(), result.currency());
    }

    @Override
    public String createShipment(String orderId, String merchantId, ShippingAddress address) {
        return shippingFulfillmentPort.createShipment(orderId, merchantId, toPortAddress(address));
    }

    private ShippingFulfillmentPort.Address toPortAddress(ShippingAddress address) {
        if (address == null) {
            return null;
        }
        return new ShippingFulfillmentPort.Address(address.fullName(), address.phone(),
                address.addressLine(), address.wardCode(), address.districtCode(),
                address.provinceCode(), address.countryCode());
    }
}
