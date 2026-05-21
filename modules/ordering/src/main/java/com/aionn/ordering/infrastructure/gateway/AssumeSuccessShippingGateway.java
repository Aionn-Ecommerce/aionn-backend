package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.ShippingGateway;
import com.aionn.ordering.domain.valueobject.ShippingAddress;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "ordering.shipping", name = "provider", havingValue = "assume-success", matchIfMissing = true)
public class AssumeSuccessShippingGateway implements ShippingGateway {

    @Override
    public ShippingQuote quote(String orderId, String merchantId, ShippingAddress address, String currency) {
        log.debug("[ASSUME-SUCCESS] quote order={} merchant={} province={}",
                orderId, merchantId, address == null ? null : address.provinceCode());
        return new ShippingQuote(new BigDecimal("30000"), currency == null ? "VND" : currency);
    }

    @Override
    public String createShipment(String orderId, String merchantId, ShippingAddress address) {
        return "ship-" + IdGenerator.ulid();
    }
}

