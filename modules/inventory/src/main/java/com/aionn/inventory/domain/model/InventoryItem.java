package com.aionn.inventory.domain.model;

import com.aionn.sharedkernel.domain.Guard;
import com.aionn.sharedkernel.domain.model.AggregateRoot;
import com.aionn.inventory.domain.event.InventoryItemEvents;
import com.aionn.inventory.domain.exception.InventoryErrorCode;
import com.aionn.inventory.domain.exception.InventoryException;
import com.aionn.inventory.domain.valueobject.AdjustmentType;
import com.aionn.inventory.domain.valueobject.InventoryItemKey;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
public class InventoryItem extends AggregateRoot {

    private final InventoryItemKey key;
    private int physicalQty;
    private int availableQty;
    private int safetyStockQty;
    private boolean locked;
    private String batchNo;
    private LocalDate expiryDate;
    private final Instant createdAt;
    private Instant updatedAt;

    public InventoryItem(
            InventoryItemKey key,
            int physicalQty,
            int availableQty,
            int safetyStockQty,
            boolean locked,
            String batchNo,
            LocalDate expiryDate,
            Instant createdAt,
            Instant updatedAt) {
        this.key = key;
        this.physicalQty = physicalQty;
        this.availableQty = availableQty;
        this.safetyStockQty = safetyStockQty;
        this.locked = locked;
        this.batchNo = batchNo;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static InventoryItem initialize(InventoryItemKey key, int initialQty) {
        Guard.require(initialQty >= 0,
                () -> new InventoryException(InventoryErrorCode.INVALID_ARGUMENT, "initialQty must be >= 0"));
        Instant now = Instant.now();
        InventoryItem item = new InventoryItem(key, initialQty, initialQty, 0,
                false, null, null, now, now);
        item.record(new InventoryItemEvents.StockInitialized(
                key.skuId(), key.warehouseId(), initialQty, now));
        return item;
    }

    public int reservedQty() {
        return physicalQty - availableQty;
    }

    public void reserve(int qty) {
        ensurePositive(qty);
        ensureUnlocked();
        Guard.require(availableQty >= qty,
                () -> new InventoryException(InventoryErrorCode.INVENTORY_INSUFFICIENT_STOCK,
                        "Available " + availableQty + " < requested " + qty));
        this.availableQty -= qty;
        touch();
    }

    public void commit(int qty) {
        ensurePositive(qty);
        // commit can run even while locked (already-paid orders should not be
        // blocked by an emergency lock â€” that is the point of locking BEFORE
        // payment), but physical must not go negative.
        Guard.require(physicalQty >= qty && reservedQty() >= qty,
                () -> new InventoryException(InventoryErrorCode.INVENTORY_NEGATIVE_PHYSICAL_STOCK,
                        "Cannot commit " + qty + ": physical=" + physicalQty + ", reserved=" + reservedQty()));
        this.physicalQty -= qty;
        touch();
    }

    public void release(int qty) {
        ensurePositive(qty);
        Guard.require(reservedQty() >= qty,
                () -> new InventoryException(InventoryErrorCode.STOCK_RESERVATION_INVALID_STATE,
                        "Cannot release more than reserved"));
        this.availableQty += qty;
        touch();
    }

    public void adjust(int signedDelta, AdjustmentType type, String reason) {
        ensureUnlocked();
        if (signedDelta == 0) {
            return;
        }
        int newPhysical = physicalQty + signedDelta;
        int newAvailable = availableQty + signedDelta;
        Guard.require(newPhysical >= 0,
                () -> new InventoryException(InventoryErrorCode.INVENTORY_NEGATIVE_PHYSICAL_STOCK));
        Guard.require(newAvailable >= 0,
                () -> new InventoryException(InventoryErrorCode.INVENTORY_INSUFFICIENT_STOCK,
                        "Available would go below zero"));
        this.physicalQty = newPhysical;
        this.availableQty = newAvailable;
        touch();
        record(new InventoryItemEvents.StockAdjusted(
                key.skuId(), key.warehouseId(), signedDelta, type.name() + ":" + (reason == null ? "" : reason),
                updatedAt));
    }

    public void configureSafetyStock(int safetyQty) {
        Guard.require(safetyQty >= 0,
                () -> new InventoryException(InventoryErrorCode.INVALID_ARGUMENT, "safetyStockQty must be >= 0"));
        this.safetyStockQty = safetyQty;
        touch();
        record(new InventoryItemEvents.SafetyStockConfigured(
                key.skuId(), key.warehouseId(), safetyQty, updatedAt, updatedAt));
        emitBreachIfApplicable();
    }

    public void emitBreachIfApplicable() {
        if (safetyStockQty > 0 && availableQty < safetyStockQty) {
            record(new InventoryItemEvents.SafetyStockBreached(
                    key.skuId(), key.warehouseId(), availableQty, safetyStockQty, Instant.now()));
        }
    }

    public void emergencyLock(String adminId, String reason) {
        this.locked = true;
        touch();
        record(new InventoryItemEvents.StockEmergencyLocked(
                key.skuId(), key.warehouseId(), adminId, reason, updatedAt, updatedAt));
    }

    public void emergencyUnlock(String adminId) {
        this.locked = false;
        touch();
        record(new InventoryItemEvents.StockEmergencyUnlocked(
                key.skuId(), key.warehouseId(), adminId, updatedAt, updatedAt));
    }

    public void trackBatchAndExpiry(String batchNo, LocalDate expiryDate) {
        Guard.require(expiryDate == null || !expiryDate.isBefore(LocalDate.now()),
                () -> new InventoryException(InventoryErrorCode.INVENTORY_EXPIRY_INVALID,
                        "expiryDate must not be in the past"));
        this.batchNo = batchNo;
        this.expiryDate = expiryDate;
        touch();
        record(new InventoryItemEvents.BatchAndExpiryTracked(
                key.skuId(), key.warehouseId(), batchNo, expiryDate, updatedAt));
    }

    
    public void recordAudit(String auditId, int actualQty) {
        Guard.require(actualQty >= 0,
                () -> new InventoryException(InventoryErrorCode.INVENTORY_AUDIT_NEGATIVE));
        int expected = physicalQty;
        Instant now = Instant.now();
        record(new InventoryItemEvents.InventoryAudited(
                auditId, key.skuId(), key.warehouseId(), expected, actualQty, now, now));
        int delta = actualQty - expected;
        if (delta != 0) {
            // Constrain available to never go negative when audit shows a
            // shortage greater than what was reserved.
            int newAvailable = Math.max(0, availableQty + delta);
            this.physicalQty = actualQty;
            this.availableQty = Math.min(actualQty, newAvailable);
            touch();
        }
    }

    private void ensurePositive(int qty) {
        Guard.require(qty > 0,
                () -> new InventoryException(InventoryErrorCode.INVALID_ARGUMENT, "qty must be > 0"));
    }

    private void ensureUnlocked() {
        Guard.require(!locked, () -> new InventoryException(InventoryErrorCode.INVENTORY_LOCKED));
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @Override
    protected String aggregateId() {
        return key.skuId() + ":" + key.warehouseId();
    }
}
