package com.aionn.inventory.adapter.rest.controller;

import com.aionn.inventory.adapter.rest.dto.reservation.ReleaseReservationRequest;
import com.aionn.inventory.adapter.rest.dto.reservation.ReserveStockRequest;
import com.aionn.inventory.application.dto.reservation.command.ReservationCommands;
import com.aionn.inventory.application.dto.reservation.result.ReservationResult;
import com.aionn.inventory.application.service.StockReservationService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * System-internal endpoints. Cross-context callers (e.g. ordering's
 * {@code InventoryStockReservationGateway}) should call the service in-process
 * — these REST endpoints are gated behind {@code ROLE_SYSTEM_ADMIN} so a
 * regular customer cannot manipulate other people's reservations.
 */
@RestController
@RequestMapping("/api/v1/inventory/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
@Tag(name = "Inventory - Reservation", description = "System-level stock reservation lifecycle")
public class StockReservationController {

    private final StockReservationService reservationService;

    @PostMapping
    @Operation(summary = "Reserve stock", description = "UC4.13 - lock available qty for a pending order")
    public ResponseEntity<ApiResponse<ReservationResult>> reserve(@Valid @RequestBody ReserveStockRequest request) {
        ReservationResult result = reservationService.reserve(new ReservationCommands.ReserveStock(
                request.skuId(), request.warehouseId(), request.orderId(), request.qty(), request.ttlSeconds()));
        return ApiResponse.createdResponse("Reservation processed", result);
    }

    @PostMapping("/{reservationId}/commit")
    @Operation(summary = "Commit reservation", description = "UC4.15 - PaymentSucceeded -> outbound recorded")
    public ResponseEntity<ApiResponse<ReservationResult>> commit(@PathVariable String reservationId) {
        ReservationResult result = reservationService.commit(
                new ReservationCommands.CommitReservation(reservationId));
        return ResponseEntity.ok(ApiResponse.success(result, "Reservation committed"));
    }

    @PostMapping("/{reservationId}/release")
    @Operation(summary = "Release reservation", description = "UC4.16 - cancel/expire releases qty back to available")
    public ResponseEntity<ApiResponse<ReservationResult>> release(
            @PathVariable String reservationId,
            @Valid @RequestBody ReleaseReservationRequest request) {
        ReservationResult result = reservationService.release(
                new ReservationCommands.ReleaseReservation(reservationId, request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Reservation released"));
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Get reservation")
    public ResponseEntity<ApiResponse<ReservationResult>> get(@PathVariable String reservationId) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.get(reservationId), "Reservation fetched"));
    }
}
