package com.aionn.inventory.application.mapper;

import com.aionn.inventory.application.dto.inventory.result.InventoryItemResult;
import com.aionn.inventory.application.dto.reservation.result.ReservationResult;
import com.aionn.inventory.application.dto.transfer.result.StockTransferResult;
import com.aionn.inventory.application.dto.warehouse.result.WarehouseResult;
import com.aionn.inventory.domain.model.InventoryItem;
import com.aionn.inventory.domain.model.StockReservation;
import com.aionn.inventory.domain.model.StockTransfer;
import com.aionn.inventory.domain.model.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class InventoryResultMapper {

    public WarehouseResult toResult(Warehouse warehouse) {
        return new WarehouseResult(
                warehouse.getWarehouseId(),
                warehouse.getMerchantId(),
                warehouse.getAddress(),
                warehouse.getPriorityLevel(),
                warehouse.getStatus().name(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt());
    }

    public InventoryItemResult toResult(InventoryItem item) {
        return new InventoryItemResult(
                item.getKey().skuId(),
                item.getKey().warehouseId(),
                item.getPhysicalQty(),
                item.getAvailableQty(),
                item.reservedQty(),
                item.getSafetyStockQty(),
                item.isLocked(),
                item.getBatchNo(),
                item.getExpiryDate(),
                item.getCreatedAt(),
                item.getUpdatedAt());
    }

    public StockTransferResult toResult(StockTransfer transfer) {
        return new StockTransferResult(
                transfer.getTransferId(),
                transfer.getMerchantId(),
                transfer.getFromWarehouseId(),
                transfer.getToWarehouseId(),
                transfer.getSkuId(),
                transfer.getQty(),
                transfer.getStatus().name(),
                transfer.getInitiatedAt(),
                transfer.getCompletedAt(),
                transfer.getCancelledAt());
    }

    public ReservationResult toResult(StockReservation reservation) {
        return new ReservationResult(
                reservation.getReservationId(),
                reservation.getSkuId(),
                reservation.getWarehouseId(),
                reservation.getOrderId(),
                reservation.getQty(),
                reservation.getStatus().name(),
                reservation.getReservedAt(),
                reservation.getExpiresAt(),
                reservation.getDecidedAt());
    }
}

