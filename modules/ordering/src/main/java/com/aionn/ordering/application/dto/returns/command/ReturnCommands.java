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
                        String ownerId,
                        BigDecimal refundAmount,
                        String currency,
                        String returnWarehouseId) implements Command {
        }

        public record RejectReturn(
                        String returnId,
                        String ownerId,
                        String reason) implements Command {
        }

        public record ConfirmItemReceived(
                        String returnId,
                        String ownerId,
                        String itemCondition) implements Command {
        }
}
