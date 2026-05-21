package com.aionn.inventory.application.service;

import com.aionn.inventory.application.dto.inventory.command.InventoryCommands;
import com.aionn.inventory.application.dto.inventory.result.InventoryItemResult;
import com.aionn.inventory.application.mapper.InventoryResultMapper;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.inventory.application.port.out.InventoryItemRepository;
import com.aionn.inventory.application.port.out.SafetyStockNotifier;
import com.aionn.inventory.application.port.out.StockAdjustmentRepository;
import com.aionn.inventory.application.port.out.WarehouseRepository;
import com.aionn.inventory.domain.event.InventoryEvent;
import com.aionn.inventory.domain.event.InventoryItemEvents;
import com.aionn.sharedkernel.domain.model.EventEnvelope;
import com.aionn.inventory.domain.exception.InventoryErrorCode;
import com.aionn.inventory.domain.exception.InventoryException;
import com.aionn.inventory.domain.model.InventoryItem;
import com.aionn.inventory.domain.model.StockAdjustment;
import com.aionn.inventory.domain.model.Warehouse;
import com.aionn.inventory.domain.valueobject.AdjustmentType;
import com.aionn.inventory.domain.valueobject.InventoryItemKey;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryItemService {

    private final InventoryItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockAdjustmentRepository adjustmentRepository;
    private final InventoryResultMapper mapper;
    private final EventPublisher eventPublisher;
    private final SafetyStockNotifier safetyStockNotifier;

    public InventoryItemResult initialize(InventoryCommands.InitializeStock command) {
        Warehouse warehouse = ownedWarehouse(command.warehouseId(), command.merchantId());
        if (!warehouse.getStatus().canFulfill()) {
            throw new InventoryException(InventoryErrorCode.WAREHOUSE_INVALID_TRANSITION,
                    "Cannot initialize stock for a non-active warehouse");
        }
        InventoryItemKey key = new InventoryItemKey(command.skuId(), command.warehouseId());
        if (itemRepository.findByKey(key).isPresent()) {
            throw new InventoryException(InventoryErrorCode.INVENTORY_ALREADY_INITIALIZED);
        }
        InventoryItem item = InventoryItem.initialize(key, command.initialQty());
        InventoryItem saved = itemRepository.save(item);
        eventPublisher.publish(item.pullEvents());
        return mapper.toResult(saved);
    }

    public InventoryItemResult configureSafetyStock(InventoryCommands.ConfigureSafetyStock command) {
        ownedWarehouse(command.warehouseId(), command.merchantId());
        InventoryItem item = lockedItem(command.skuId(), command.warehouseId());
        item.configureSafetyStock(command.safetyStockQty());
        InventoryItem saved = itemRepository.save(item);
        publishWithSafetyHook(item.pullEvents(), command.merchantId());
        return mapper.toResult(saved);
    }

    public InventoryItemResult trackBatchAndExpiry(InventoryCommands.TrackBatchAndExpiry command) {
        ownedWarehouse(command.warehouseId(), command.merchantId());
        InventoryItem item = lockedItem(command.skuId(), command.warehouseId());
        item.trackBatchAndExpiry(command.batchNo(), command.expiryDate());
        InventoryItem saved = itemRepository.save(item);
        eventPublisher.publish(item.pullEvents());
        return mapper.toResult(saved);
    }

    public InventoryItemResult manualAdjustment(InventoryCommands.ManualAdjustment command) {
        ownedWarehouse(command.warehouseId(), command.merchantId());
        InventoryItem item = lockedItem(command.skuId(), command.warehouseId());

        int signedDelta = switch (command.type()) {
            case MANUAL_INCREASE, TRANSFER_IN -> Math.abs(command.qty());
            case MANUAL_DECREASE, DAMAGED, TRANSFER_OUT -> -Math.abs(command.qty());
            case OUTBOUND -> throw new InventoryException(InventoryErrorCode.STOCK_ADJUSTMENT_INVALID,
                    "Use commit reservation for OUTBOUND");
        };
        item.adjust(signedDelta, command.type(), command.reason());
        item.emitBreachIfApplicable();
        InventoryItem saved = itemRepository.save(item);

        StockAdjustment adjustment = StockAdjustment.manual(IdGenerator.ulid(),
                command.skuId(), command.warehouseId(), Math.abs(signedDelta), command.type(), command.reason());
        adjustmentRepository.save(adjustment);
        eventPublisher.publish(adjustment.pullEvents());

        publishWithSafetyHook(item.pullEvents(), command.merchantId());
        return mapper.toResult(saved);
    }

    public InventoryItemResult emergencyLock(InventoryCommands.EmergencyLock command) {
        InventoryItem item = lockedItem(command.skuId(), command.warehouseId());
        item.emergencyLock(command.adminId(), command.reason());
        InventoryItem saved = itemRepository.save(item);
        eventPublisher.publish(item.pullEvents());
        return mapper.toResult(saved);
    }

    public InventoryItemResult emergencyUnlock(InventoryCommands.EmergencyUnlock command) {
        InventoryItem item = lockedItem(command.skuId(), command.warehouseId());
        item.emergencyUnlock(command.adminId());
        InventoryItem saved = itemRepository.save(item);
        eventPublisher.publish(item.pullEvents());
        return mapper.toResult(saved);
    }

    public InventoryItemResult auditInventory(InventoryCommands.AuditInventory command) {
        ownedWarehouse(command.warehouseId(), command.merchantId());
        InventoryItem item = lockedItem(command.skuId(), command.warehouseId());
        item.recordAudit(IdGenerator.ulid(), command.actualQty());
        item.emitBreachIfApplicable();
        InventoryItem saved = itemRepository.save(item);
        publishWithSafetyHook(item.pullEvents(), command.merchantId());
        return mapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public InventoryItemResult get(String skuId, String warehouseId) {
        return itemRepository.findByKey(new InventoryItemKey(skuId, warehouseId))
                .map(mapper::toResult)
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND));
    }

    private Warehouse ownedWarehouse(String warehouseId, String merchantId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.WAREHOUSE_NOT_FOUND));
        warehouse.ensureOwnedBy(merchantId);
        return warehouse;
    }

    private InventoryItem lockedItem(String skuId, String warehouseId) {
        return itemRepository.lockByKey(new InventoryItemKey(skuId, warehouseId))
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND));
    }

    @SuppressWarnings("unchecked")
    private void publishWithSafetyHook(List<EventEnvelope> events, String merchantId) {
        eventPublisher.publish(events);
        for (EventEnvelope envelope : events) {
            if (envelope.payload() instanceof InventoryItemEvents.SafetyStockBreached breach) {
                safetyStockNotifier.notifySafetyStockBreach(merchantId,
                        breach.skuId(), breach.warehouseId(), breach.availableQty(), breach.safetyStockQty());
            }
        }
    }
}
