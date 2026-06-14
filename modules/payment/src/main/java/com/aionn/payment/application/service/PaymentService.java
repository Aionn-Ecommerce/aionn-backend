package com.aionn.payment.application.service;

import com.aionn.payment.application.dto.payment.command.ConfirmPaymentCommand;
import com.aionn.payment.application.dto.payment.command.FailPaymentCommand;
import com.aionn.payment.application.dto.payment.command.InitiatePaymentCommand;
import com.aionn.payment.application.dto.payment.command.RefundPaymentCommand;
import com.aionn.payment.application.dto.payment.result.PaymentResult;
import com.aionn.payment.application.mapper.PaymentResultMapper;
import com.aionn.payment.application.port.out.InvoiceStorage;
import com.aionn.payment.application.port.out.PaymentMethodPersistencePort;
import com.aionn.payment.application.port.out.PaymentProviderClient;
import com.aionn.payment.application.port.out.PaymentProviderRouter;
import com.aionn.payment.application.port.out.PaymentPersistencePort;
import com.aionn.payment.application.port.out.TransactionLedgerPersistencePort;
import com.aionn.payment.application.port.out.integration.PaymentIntegrationEventPublisherPort;
import com.aionn.payment.domain.exception.PaymentErrorCode;
import com.aionn.payment.domain.exception.PaymentException;
import com.aionn.payment.domain.model.Payment;
import com.aionn.payment.domain.model.PaymentMethod;
import com.aionn.payment.domain.model.TransactionLedger;
import com.aionn.payment.domain.valueobject.LedgerEntryType;
import com.aionn.payment.domain.valueobject.PaymentMethodStatus;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentPersistencePort paymentRepository;
    private final PaymentMethodPersistencePort paymentMethodRepository;
    private final TransactionLedgerPersistencePort ledgerRepository;
    private final PaymentProviderRouter providerRouter;
    private final InvoiceStorage invoiceStorage;
    private final PaymentResultMapper mapper;
    private final EventPublisher eventPublisher;
    private final PaymentIntegrationEventPublisherPort integrationEventPublisher;

    public PaymentResult initiate(InitiatePaymentCommand command) {
        var existing = paymentRepository.findByIdempotencyKey(command.idempotencyKey());
        if (existing.isPresent()) {
            return mapper.toResult(existing.get());
        }

        PaymentMethod method = null;
        if (command.paymentMethodId() != null) {
            method = paymentMethodRepository.findById(command.paymentMethodId())
                    .orElseThrow(() -> new PaymentException(PaymentErrorCode.METHOD_NOT_FOUND));
            method.ensureOwnedBy(command.userId());
            if (method.getStatus() != PaymentMethodStatus.VERIFIED) {
                throw new PaymentException(PaymentErrorCode.METHOD_NOT_VERIFIED);
            }
        }

        Money amount = Money.of(command.amount(), command.currency());
        Payment payment = Payment.initiate(IdGenerator.ulid(), command.orderId(), command.userId(),
                command.paymentMethodId(), amount, command.gateway(), command.idempotencyKey());
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publish(payment.pullEvents());

        PaymentProviderClient client = providerRouter.route(command.gateway());
        PaymentProviderClient.Authorization auth = client.authorize(
                new PaymentProviderClient.AuthorizationRequest(
                        saved.getPaymentId(), command.orderId(), command.userId(),
                        method == null ? null : method.getGatewayToken(),
                        command.amount(), command.currency(), command.idempotencyKey(), null));

        if (auth.captured()) {
            return confirm(new ConfirmPaymentCommand(saved.getPaymentId(), auth.transactionNo()));
        } else if (auth.declineCode() != null) {
            return fail(new FailPaymentCommand(saved.getPaymentId(),
                    auth.declineCode(), auth.declineReason()));
        }
        // Async path: return INITIATED and propagate the redirect URL for client
        // hand-off.
        return mapper.toResult(saved).withRedirectUrl(auth.authUrl());
    }

    public PaymentResult confirm(ConfirmPaymentCommand command) {
        Payment payment = required(command.paymentId());
        if (payment.getStatus().name().equals("PAID")) {
            return mapper.toResult(payment);
        }
        payment.markPaid(command.transactionNo());
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publish(payment.pullEvents());

        TransactionLedger entry = TransactionLedger.record(IdGenerator.ulid(),
                saved.getPaymentId(), saved.getAmount(), LedgerEntryType.CREDIT,
                saved.getGateway().name(), command.transactionNo());
        ledgerRepository.save(entry);
        eventPublisher.publish(entry.pullEvents());

        String invoiceUrl = invoiceStorage.storeInvoiceUrl(saved.getPaymentId(), saved.getOrderId());
        saved.attachInvoice(invoiceUrl);
        Payment withInvoice = paymentRepository.save(saved);
        eventPublisher.publish(saved.pullEvents());

        integrationEventPublisher.publishPaymentCaptured(withInvoice.getPaymentId(), withInvoice.getOrderId(),
                command.transactionNo(), withInvoice.getAmount().amount(), withInvoice.getAmount().currency());
        return mapper.toResult(withInvoice);
    }

    public PaymentResult fail(FailPaymentCommand command) {
        Payment payment = required(command.paymentId());
        payment.markFailed(command.errorCode(), command.reason());
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publish(payment.pullEvents());
        integrationEventPublisher.publishPaymentFailed(saved.getPaymentId(), saved.getOrderId(),
                command.errorCode(), command.reason());
        return mapper.toResult(saved);
    }

    public PaymentResult refund(RefundPaymentCommand command) {
        Payment payment = required(command.paymentId());
        Money refund = Money.of(command.amount(), command.currency());

        PaymentProviderClient client = providerRouter.route(payment.getGateway());
        PaymentProviderClient.Refund providerRefund = client.refund(new PaymentProviderClient.RefundRequest(
                payment.getPaymentId(), payment.getTransactionNo(), command.amount(), command.currency(),
                command.reason()));
        if (!providerRefund.accepted()) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR,
                    "Refund declined: " + providerRefund.declineReason());
        }

        String refundId = providerRefund.refundTransactionNo() != null
                ? providerRefund.refundTransactionNo()
                : "refund-" + IdGenerator.ulid();
        payment.refund(refundId, refund, command.reason());
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publish(payment.pullEvents());

        TransactionLedger entry = TransactionLedger.record(IdGenerator.ulid(),
                saved.getPaymentId(), refund, LedgerEntryType.DEBIT,
                saved.getGateway().name(), refundId);
        ledgerRepository.save(entry);
        eventPublisher.publish(entry.pullEvents());

        integrationEventPublisher.publishPaymentRefunded(saved.getPaymentId(), saved.getOrderId(),
                refundId, command.amount(), command.currency(), command.reason());
        return mapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResult get(String paymentId) {
        return mapper.toResult(required(paymentId));
    }

    Payment required(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }
}
