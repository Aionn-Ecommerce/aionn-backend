package com.aionn.inventory.application.dto.transfer.command;

import com.aionn.sharedkernel.application.command.Command;

public final class StockTransferCommands {

        private StockTransferCommands() {
        }

        public record InitiateTransfer(
                        String ownerId,
                        String fromWarehouseId,
                        String toWarehouseId,
                        String skuId,
                        int qty) implements Command {
        }

        public record CompleteTransfer(
                        String ownerId,
                        String transferId,
                        int receivedQty) implements Command {
        }

        public record CancelTransfer(
                        String ownerId,
                        String transferId,
                        String reason) implements Command {
        }
}
