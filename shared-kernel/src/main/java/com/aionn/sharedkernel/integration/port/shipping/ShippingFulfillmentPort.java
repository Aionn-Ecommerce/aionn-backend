package com.aionn.sharedkernel.integration.port.shipping;

import java.math.BigDecimal;

public interface ShippingFulfillmentPort {

    QuoteResult quote(String orderId, String merchantId, Address address, String currency);

    String createShipment(String orderId, String merchantId, Address address);

    record Address(
            String fullName,
            String phone,
            String addressLine,
            String wardCode,
            String districtCode,
            String provinceCode,
            String countryCode) {
    }

    record QuoteResult(BigDecimal fee, String currency) {
    }
}
