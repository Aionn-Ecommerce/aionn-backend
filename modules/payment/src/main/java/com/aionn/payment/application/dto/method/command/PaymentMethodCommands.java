package com.aionn.payment.application.dto.method.command;

import com.aionn.sharedkernel.application.command.Command;

public final class PaymentMethodCommands {

    private PaymentMethodCommands() {
    }

    public record LinkMethod(
            String userId,
            String provider,
            String last4Digits,
            String gatewayToken) implements Command {
    }

    public record VerifyMethod(String userId, String methodId) implements Command {
    }

    public record RemoveMethod(String userId, String methodId) implements Command {
    }
}
