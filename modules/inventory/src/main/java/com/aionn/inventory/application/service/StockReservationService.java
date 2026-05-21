package com.aionn.inventory.application.service;

import com.aionn.inventory.application.dto.reservation.command.ReservationCommands;
import com.aionn.inventory.application.dto.reservation.result.ReservationResult;
import com.aionn.inventory.application.mapper.InventoryResultMapper;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.inventory.application.port.out.InventoryItemRepository;
import com.aionn.inventory.application.port.out.OutboundOrderNotifier;
import com.aionn.inventory.application.port.out.StockAdjustmentRepository;
import com.aionn.inventory.application.port.out.StockReservationRepository;
import com.aionn.inventory.domain.exception.InventoryErrorCode;
import com.aionn.inventory.domain.exception.InventoryException;
import com.aionn.inventory.domain.model.InventoryItem;
import com.aionn.inventory.domain.model.StockAdjustment;
import com.aionn.inventory.domain.model.StockReservation;
import com.aionn.inventory.domain.valueobject.InventoryItemKey;
import com.aionn.inventory.domain.valueobject.ReservationStatus;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StockReservationService {

    private static final int DEFAULT_AUTO_RELEASE_BATCH = 100;

    private final InventoryItemRepository itemRepository;
    private final StockReservationRepository reservationRepository;
    private final StockAdjustmentRepository adjustmentRepository;
    private final InventoryResultMapper mapper;
    private final EventPublisher eventPublisher;
    private final OutboundOrderNotifier outboundOrderNotifier;

    public ReservationResult reserve(ReservationCommands.ReserveStock command) {
        InventoryItemKey key = new InventoryItemKey(command.skuId(), command.warehouseId());
        InventoryItem item = itemRepository.lockByKey(key)
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND));

        if (item.isLocked() || item.getAvailableQty() < command.qty()) {
            // Persist a FAILED reservation so the event is auditable, then
            // notify Ordering so the cart proposal can be rejected.
            String reason = item.isLocked() ? "Inventory locked" : "Insufficient stock";
            StockReservation failed = StockReservation.failed(IdGenerator.ulid(),
                    command.skuId(), command.warehouseId(), command.qty(), reason);
            StockReservation saved = reservationRepository.save(failed);
            eventPublisher.publish(failed.pullEvents());
            outboundOrderNotifier.notifyReservationFailed(command.orderId(),
                    command.skuId(), command.warehouseId(), command.qty(), reason);
            return mapper.toResult(saved);
        }

        item.reserve(command.qty());
        itemRepository.save(item);

        Instant expiresAt = Instant.now().plus(Duration.ofSeconds(command.ttlSeconds()));
        StockReservation reservation = StockReservation.reserve(IdGenerator.ulid(),
                command.skuId(), command.warehouseId(), command.orderId(), command.qty(), expiresAt);
        StockReservation saved = reservationRepository.save(reservation);
        eventPublisher.publish(reservation.pullEvents());
        eventPublisher.publish(item.pullEvents());
        return mapper.toResult(saved);
    }

    public ReservationResult commit(ReservationCommands.CommitReservation command) {
        StockReservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.STOCK_RESERVATION_NOT_FOUND));
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new InventoryException(InventoryErrorCode.STOCK_RESERVATION_INVALID_STATE);
        }
        InventoryItem item = itemRepository.lockByKey(
                new InventoryItemKey(reservation.getSkuId(), reservation.getWarehouseId()))
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND));

        item.commit(reservation.getQty());
        itemRepository.save(item);

        reservation.commit();
        StockReservation saved = reservationRepository.save(reservation);

        StockAdjustment outbound = StockAdjustment.outbound(IdGenerator.ulid(),
                reservation.getSkuId(), reservation.getWarehouseId(), reservation.getQty(),
                reservation.getOrderId());
        adjustmentRepository.save(outbound);

        eventPublisher.publish(reservation.pullEvents());
        eventPublisher.publish(item.pullEvents());
        eventPublisher.publish(outbound.pullEvents());
        outboundOrderNotifier.notifyOutbound(reservation.getOrderId(),
                reservation.getSkuId(), reservation.getWarehouseId(), reservation.getQty());
        return mapper.toResult(saved);
    }

    public ReservationResult release(ReservationCommands.ReleaseReservation command) {
        StockReservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.STOCK_RESERVATION_NOT_FOUND));
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new InventoryException(InventoryErrorCode.STOCK_RESERVATION_INVALID_STATE);
        }
        InventoryItem item = itemRepository.lockByKey(
                new InventoryItemKey(reservation.getSkuId(), reservation.getWarehouseId()))
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND));

        item.release(reservation.getQty());
        itemRepository.save(item);

        reservation.release(command.reason());
        StockReservation saved = reservationRepository.save(reservation);

        eventPublisher.publish(reservation.pullEvents());
        eventPublisher.publish(item.pullEvents());
        return mapper.toResult(saved);
    }

    
    public int autoReleaseExpired(Instant now, int batchSize) {
        int limit = batchSize > 0 ? batchSize : DEFAULT_AUTO_RELEASE_BATCH;
        List<StockReservation> expired = reservationRepository.findExpired(now, limit);
        int released = 0;
        for (StockReservation reservation : expired) {
            try {
                release(new ReservationCommands.ReleaseReservation(reservation.getReservationId(), "expired"));
                released++;
            } catch (InventoryException ex) {
                log.warn("Skip expired reservation {}: {}", reservation.getReservationId(), ex.getMessage());
            }
        }
        return released;
    }

    @Transactional(readOnly = true)
    public ReservationResult get(String reservationId) {
        return mapper.toResult(reservationRepository.findById(reservationId)
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.STOCK_RESERVATION_NOT_FOUND)));
    }
}

