package com.aionn.payment.infrastructure.listener;

import com.aionn.payment.application.dto.payment.command.RefundPaymentCommand;
import com.aionn.payment.application.port.out.PaymentPersistencePort;
import com.aionn.payment.application.service.PaymentService;
import com.aionn.payment.domain.model.Payment;
import com.aionn.payment.domain.valueobject.PaymentStatus;
import com.aionn.sharedkernel.integration.event.ordering.OrderCancelledIntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component("paymentOrderLifecycleListener")
@RequiredArgsConstructor
public class OrderLifecycleListener {

    private final PaymentPersistencePort paymentRepository;
    private final PaymentService paymentService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderCancelled(OrderCancelledIntegrationEvent event) {
        String reason = event.cancellationType().name() + ":" + event.reasonCode();
        autoRefundPaidPayments(event.orderId(), reason);
    }

    private void autoRefundPaidPayments(String orderId, String reason) {
        for (Payment payment : paymentRepository.findByOrderId(orderId)) {
            if (payment.getStatus() != PaymentStatus.PAID) {
                continue;
            }
            try {
                paymentService.refund(new RefundPaymentCommand(payment.getPaymentId(),
                        payment.getAmount().amount(), payment.getAmount().currency(), reason));
            } catch (Exception ex) {
                log.warn("Auto-refund for payment {} failed: {}", payment.getPaymentId(), ex.getMessage());
            }
        }
    }
}
