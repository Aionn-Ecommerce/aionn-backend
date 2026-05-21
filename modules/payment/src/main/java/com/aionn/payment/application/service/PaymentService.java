package com.aionn.payment.application.service;

import com.aionn.payment.application.dto.payment.command.PaymentCommands;
import com.aionn.payment.application.dto.payment.result.PaymentResult;
import com.aionn.payment.application.mapper.PaymentResultMapper;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.payment.application.port.out.InvoiceStorage;
import com.aionn.payment.application.port.out.PaymentMethodRepository;
import com.aionn.payment.application.port.out.PaymentProviderClient;
import com.aionn.payment.application.port.out.PaymentProviderRouter;
import com.aionn.payment.application.port.out.PaymentRepository;
import com.aionn.payment.application.port.out.TransactionLedgerRepository;
import com.aionn.payment.domain.exception.PaymentErrorCode;
import com.aionn.payment.domain.exception.PaymentException;
import com.aionn.payment.domain.model.Payment;
import com.aionn.payment.domain.model.PaymentMethod;
import com.aionn.payment.domain.model.TransactionLedger;
import com.aionn.payment.domain.valueobject.LedgerEntryType;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.payment.domain.valueobject.PaymentMethodStatus;
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

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final TransactionLedgerRepository ledgerRepository;
    private final PaymentProviderRouter providerRouter;
    private final InvoiceStorage invoiceStorage;
    private final PaymentResultMapper mapper;
    private final EventPublisher eventPublisher;

    public PaymentResult initiate(PaymentCommands.InitiatePayment command) {
        // Idempotency: if a payment with this key already exists, return it.
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

        // Authorize via provider
        PaymentProviderClient client = providerRouter.route(command.gateway());
        PaymentProviderClient.Authorization auth = client.authorize(
                new PaymentProviderClient.AuthorizationRequest(
                        saved.getPaymentId(), command.orderId(), command.userId(),
                        method == null ? null : method.getGatewayToken(),
                        command.amount(), command.currency(), command.idempotencyKey(), null));

        if (auth.captured()) {
            return confirm(new PaymentCommands.ConfirmPayment(saved.getPaymentId(), auth.transactionNo()));
        } else if (auth.declineCode() != null) {
            return fail(new PaymentCommands.FailPayment(saved.getPaymentId(),
                    auth.declineCode(), auth.declineReason()));
        }
        // Async path (e.g. VNPay redirect): return as INITIATED
        return mapper.toResult(saved);
    }

    public PaymentResult confirm(PaymentCommands.ConfirmPayment command) {
        Payment payment = required(command.paymentId());
        if (payment.getStatus().name().equals("PAID")) {
            // idempotent re-confirm
            return mapper.toResult(payment);
        }
        payment.markPaid(command.transactionNo());
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publish(payment.pullEvents());

        // Ledger entry
        TransactionLedger entry = TransactionLedger.record(IdGenerator.ulid(),
                saved.getPaymentId(), saved.getAmount(), LedgerEntryType.CREDIT,
                saved.getGateway().name(), command.transactionNo());
        ledgerRepository.save(entry);
        eventPublisher.publish(entry.pullEvents());

        // Generate invoice
        String invoiceUrl = invoiceStorage.storeInvoiceUrl(saved.getPaymentId(), saved.getOrderId());
        saved.attachInvoice(invoiceUrl);
        Payment withInvoice = paymentRepository.save(saved);
        eventPublisher.publish(saved.pullEvents());
        return mapper.toResult(withInvoice);
    }

    public PaymentResult fail(PaymentCommands.FailPayment command) {
        Payment payment = required(command.paymentId());
        payment.markFailed(command.errorCode(), command.reason());
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publish(payment.pullEvents());
        return mapper.toResult(saved);
    }

    public PaymentResult refund(PaymentCommands.RefundPayment command) {
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

