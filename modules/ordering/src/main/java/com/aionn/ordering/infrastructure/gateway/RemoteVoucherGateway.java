package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.VoucherGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(prefix = "ordering.voucher", name = "provider", havingValue = "remote")
public class RemoteVoucherGateway implements VoucherGateway {

    @Override
    public Discount apply(String userId, String voucherCode, BigDecimal lineSubtotal, String currency) {
        throw new UnsupportedOperationException("Remote VoucherGateway is not implemented yet");
    }
}

