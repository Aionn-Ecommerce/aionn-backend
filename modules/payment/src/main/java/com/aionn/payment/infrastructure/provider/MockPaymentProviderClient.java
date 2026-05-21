package com.aionn.payment.infrastructure.provider;

import com.aionn.payment.application.port.out.PaymentProviderClient;
import com.aionn.payment.domain.valueobject.PaymentGatewayKind;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Always-success provider used in dev/test. Wired by default; switch to
 * Stripe / VNPay by setting their {@code .enabled=true} property.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "payment.provider.mock", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MockPaymentProviderClient implements PaymentProviderClient {

    @Override
    public PaymentGatewayKind kind() {
        return PaymentGatewayKind.MOCK;
    }

    @Override
    public Authorization authorize(AuthorizationRequest request) {
        log.info("[MOCK] authorize order={} amount={} {}", request.orderId(), request.amount(), request.currency());
        return new Authorization(true, "mock-txn-" + IdGenerator.ulid(), null, null, null);
    }

    @Override
    public Refund refund(RefundRequest request) {
        log.info("[MOCK] refund payment={} amount={} {}", request.paymentId(), request.amount(),
                request.currency());
        return new Refund(true, "mock-refund-" + IdGenerator.ulid(), null);
    }

    @Override
    public String generateInvoice(String paymentId, String orderId, BigDecimal amount, String currency) {
        return "https://invoices.test/mock/" + paymentId + ".pdf";
    }

    @Override
    public WebhookEvent verifyAndParse(String rawBody, String signatureHeader) {
        // Mock provider treats the body as JSON-ish "paymentId|transactionNo".
        String[] parts = rawBody.split("\\|");
        if (parts.length < 2) {
            return new WebhookEvent("payment.unknown", null, null, null, null, false,
                    "INVALID_PAYLOAD", "Mock webhook expected paymentId|transactionNo");
        }
        return new WebhookEvent("payment.processed", parts[0], parts[1], null, null, true, null, null);
    }
}

