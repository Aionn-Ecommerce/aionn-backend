package com.aionn.inventory.application.service;

import com.aionn.inventory.application.dto.warehouse.command.WarehouseCommands;
import com.aionn.inventory.application.dto.warehouse.result.WarehouseResult;
import com.aionn.inventory.application.mapper.InventoryResultMapper;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.inventory.application.port.out.WarehouseRepository;
import com.aionn.inventory.domain.exception.InventoryErrorCode;
import com.aionn.inventory.domain.exception.InventoryException;
import com.aionn.inventory.domain.model.Warehouse;
import com.aionn.inventory.domain.valueobject.WarehouseStatus;
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
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryResultMapper mapper;
    private final EventPublisher eventPublisher;

    public WarehouseResult create(WarehouseCommands.CreateWarehouse command) {
        Warehouse warehouse = Warehouse.create(IdGenerator.ulid(),
                command.merchantId(), command.address(), command.priorityLevel());
        Warehouse saved = warehouseRepository.save(warehouse);
        eventPublisher.publish(warehouse.pullEvents());
        return mapper.toResult(saved);
    }

    public WarehouseResult changeStatus(WarehouseCommands.ChangeStatus command) {
        Warehouse warehouse = ownedBy(command.warehouseId(), command.merchantId());
        WarehouseStatus next;
        try {
            next = WarehouseStatus.valueOf(command.status());
        } catch (IllegalArgumentException ex) {
            throw new InventoryException(InventoryErrorCode.INVALID_ARGUMENT,
                    "Unknown status: " + command.status());
        }
        warehouse.changeStatus(next);
        Warehouse saved = warehouseRepository.save(warehouse);
        eventPublisher.publish(warehouse.pullEvents());
        return mapper.toResult(saved);
    }

    public WarehouseResult adjustPriority(WarehouseCommands.AdjustPriority command) {
        Warehouse warehouse = ownedBy(command.warehouseId(), command.merchantId());
        warehouse.adjustPriority(command.priorityLevel());
        Warehouse saved = warehouseRepository.save(warehouse);
        eventPublisher.publish(warehouse.pullEvents());
        return mapper.toResult(saved);
    }

    public WarehouseResult suspend(WarehouseCommands.SuspendWarehouse command) {
        Warehouse warehouse = required(command.warehouseId());
        warehouse.suspend(command.adminId(), command.reason());
        Warehouse saved = warehouseRepository.save(warehouse);
        eventPublisher.publish(warehouse.pullEvents());
        return mapper.toResult(saved);
    }

    public WarehouseResult liftSuspension(WarehouseCommands.LiftSuspension command) {
        Warehouse warehouse = required(command.warehouseId());
        warehouse.liftSuspension();
        Warehouse saved = warehouseRepository.save(warehouse);
        eventPublisher.publish(warehouse.pullEvents());
        return mapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public WarehouseResult get(String warehouseId) {
        return mapper.toResult(required(warehouseId));
    }

    @Transactional(readOnly = true)
    public List<WarehouseResult> listByMerchant(String merchantId) {
        return warehouseRepository.findByMerchantOrderByPriority(merchantId).stream()
                .map(mapper::toResult)
                .toList();
    }

    private Warehouse required(String warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.WAREHOUSE_NOT_FOUND));
    }

    private Warehouse ownedBy(String warehouseId, String merchantId) {
        Warehouse warehouse = required(warehouseId);
        warehouse.ensureOwnedBy(merchantId);
        return warehouse;
    }
}

