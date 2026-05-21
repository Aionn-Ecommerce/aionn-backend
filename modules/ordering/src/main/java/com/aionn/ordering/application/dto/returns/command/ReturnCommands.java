package com.aionn.ordering.application.dto.returns.command;

import com.aionn.sharedkernel.application.command.Command;

import java.math.BigDecimal;

public final class ReturnCommands {

        private ReturnCommands() {
        }

        public record RequestReturn(
                        String orderId,
                        String userId,
                        String reason,
                        String evidenceUrl) implements Command {
        }

        public record ApproveReturn(
                        String returnId,
                        String merchantId,
                        BigDecimal refundAmount,
                        String currency,
                        String returnWarehouseId) implements Command {
        }

        public record RejectReturn(
                        String returnId,
                        String merchantId,
                        String reason) implements Command {
        }

        public record ConfirmItemReceived(
                        String returnId,
                        String merchantId,
                        String itemCondition) implements Command {
        }
}
