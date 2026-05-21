package com.aionn.inventory.application.dto.warehouse.command;

import com.aionn.sharedkernel.application.command.Command;

public final class WarehouseCommands {

    private WarehouseCommands() {
    }

    public record CreateWarehouse(String merchantId, String address, int priorityLevel) implements Command {
    }

    public record ChangeStatus(String warehouseId, String merchantId, String status) implements Command {
    }

    public record AdjustPriority(String warehouseId, String merchantId, int priorityLevel) implements Command {
    }

    public record SuspendWarehouse(String warehouseId, String adminId, String reason) implements Command {
    }

    public record LiftSuspension(String warehouseId, String adminId) implements Command {
    }
}
