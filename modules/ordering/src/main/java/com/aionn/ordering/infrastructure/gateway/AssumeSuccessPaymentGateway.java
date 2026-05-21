package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.PaymentGateway;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Default payment adapter for dev/test. Always approves. Replace with the
 * real remote when Payment is implemented.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "ordering.payment", name = "provider", havingValue = "assume-success", matchIfMissing = true)
public class AssumeSuccessPaymentGateway implements PaymentGateway {

    @Override
    public PaymentAuthorization authorize(String orderId, String userId, String paymentMethodId,
            BigDecimal amount, String currency) {
        String paymentId = "pay-" + IdGenerator.ulid();
        log.info("[ASSUME-SUCCESS] authorize order={} user={} method={} amount={} {}",
                orderId, userId, paymentMethodId, amount, currency);
        return new PaymentAuthorization(paymentId, true, null);
    }

    @Override
    public void refund(String paymentId, BigDecimal amount, String currency, String reason) {
        log.info("[ASSUME-SUCCESS] refund paymentId={} amount={} {} reason={}",
                paymentId, amount, currency, reason);
    }
}

