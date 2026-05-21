package com.aionn.payment.application.dto.payment.command;

import com.aionn.payment.domain.valueobject.PaymentGatewayKind;
import com.aionn.sharedkernel.application.command.Command;

import java.math.BigDecimal;

public final class PaymentCommands {

        private PaymentCommands() {
        }

        public record InitiatePayment(
                        String orderId,
                        String userId,
                        String paymentMethodId,
                        BigDecimal amount,
                        String currency,
                        PaymentGatewayKind gateway,
                        String idempotencyKey) implements Command {
        }

        public record ConfirmPayment(
                        String paymentId,
                        String transactionNo) implements Command {
        }

        public record FailPayment(
                        String paymentId,
                        String errorCode,
                        String reason) implements Command {
        }

        public record RefundPayment(
                        String paymentId,
                        BigDecimal amount,
                        String currency,
                        String reason) implements Command {
        }
}
