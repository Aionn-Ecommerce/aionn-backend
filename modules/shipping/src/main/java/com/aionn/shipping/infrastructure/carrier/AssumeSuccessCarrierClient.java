package com.aionn.shipping.infrastructure.carrier;

import com.aionn.shipping.application.port.out.CarrierClient;
import com.aionn.shipping.domain.valueobject.ShipmentAddress;
import com.aionn.shipping.domain.valueobject.ShipmentDimensions;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "shipping.carrier", name = "provider", havingValue = "assume-success", matchIfMissing = true)
public class AssumeSuccessCarrierClient implements CarrierClient {

    @Override
    public Quote quote(ShipmentAddress address, ShipmentDimensions dimensions, String currency) {
        // Flat 30 000 VND for any address, mock zone code = province.
        return new Quote(new BigDecimal("30000"), currency == null ? "VND" : currency,
                address.provinceCode(), "assume-success flat rate");
    }

    @Override
    public Registration register(String shipmentId, String orderId, ShipmentAddress address,
            ShipmentDimensions dimensions, BigDecimal codAmount, BigDecimal shippingFee, String currency) {
        return new Registration("MOCK-" + IdGenerator.ulid(), "carrier-" + IdGenerator.ulid(),
                Instant.now().plus(3, ChronoUnit.DAYS));
    }

    @Override
    public String fetchLabel(String trackingCode) {
        return "https://labels.test/mock/" + trackingCode + ".pdf";
    }

    @Override
    public void cancel(String trackingCode, String reason) {
        log.info("[ASSUME-SUCCESS] cancel tracking={} reason={}", trackingCode, reason);
    }
}

