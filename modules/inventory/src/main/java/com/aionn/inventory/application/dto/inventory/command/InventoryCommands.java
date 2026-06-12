package com.aionn.inventory.application.dto.inventory.command;

import com.aionn.inventory.domain.valueobject.AdjustmentType;
import com.aionn.sharedkernel.application.command.Command;

import java.time.LocalDate;

public final class InventoryCommands {

        private InventoryCommands() {
        }

        public record InitializeStock(
                        String ownerId,
                        String skuId,
                        String warehouseId,
                        int initialQty) implements Command {
        }

        public record ConfigureSafetyStock(
                        String ownerId,
                        String skuId,
                        String warehouseId,
                        int safetyStockQty) implements Command {
        }

        public record TrackBatchAndExpiry(
                        String ownerId,
                        String skuId,
                        String warehouseId,
                        String batchNo,
                        LocalDate expiryDate) implements Command {
        }

        public record ManualAdjustment(
                        String ownerId,
                        String skuId,
                        String warehouseId,
                        int qty,
                        AdjustmentType type,
                        String reason) implements Command {
        }

        public record EmergencyLock(
                        String adminId,
                        String skuId,
                        String warehouseId,
                        String reason) implements Command {
        }

        public record EmergencyUnlock(
                        String adminId,
                        String skuId,
                        String warehouseId) implements Command {
        }

        public record AuditInventory(
                        String ownerId,
                        String skuId,
                        String warehouseId,
                        int actualQty) implements Command {
        }
}
