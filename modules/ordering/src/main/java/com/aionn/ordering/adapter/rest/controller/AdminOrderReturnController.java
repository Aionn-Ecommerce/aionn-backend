package com.aionn.ordering.adapter.rest.controller;

import com.aionn.ordering.adapter.rest.dto.returns.AdminApproveReturnRequest;
import com.aionn.ordering.adapter.rest.dto.returns.AdminConfirmReturnReceivedRequest;
import com.aionn.ordering.adapter.rest.dto.returns.AdminRejectReturnRequest;
import com.aionn.ordering.application.dto.returns.result.ReturnResult;
import com.aionn.ordering.application.service.OrderReturnService;
import com.aionn.ordering.domain.valueobject.ReturnStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/ordering/returns")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
@Tag(name = "Ordering - Return Admin",
        description = "Admin override for buyer-merchant return disputes (force approve/reject/receive)")
public class AdminOrderReturnController {

    private final OrderReturnService returnService;

    @GetMapping
    @Operation(summary = "List returns by status (admin)")
    public ResponseEntity<ApiResponse<List<ReturnResult>>> listByStatus(
            @RequestParam(defaultValue = "REQUESTED") ReturnStatus status,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                returnService.adminListByStatus(status, limit), "Returns fetched"));
    }

    @GetMapping("/{returnId}")
    @Operation(summary = "Get return (admin)")
    public ResponseEntity<ApiResponse<ReturnResult>> get(@PathVariable String returnId) {
        return ResponseEntity.ok(ApiResponse.success(
                returnService.adminGet(returnId), "Return fetched"));
    }

    @PostMapping("/{returnId}/approve")
    @Operation(summary = "Force-approve return (admin)",
            description = "Admin override when merchant fails to act on a buyer-initiated return")
    public ResponseEntity<ApiResponse<ReturnResult>> approve(
            @PathVariable String returnId,
            @Valid @RequestBody AdminApproveReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                returnService.adminApprove(returnId, request.refundAmount(),
                        request.currency(), request.returnWarehouseId()),
                "Return approved by admin"));
    }

    @PostMapping("/{returnId}/reject")
    @Operation(summary = "Force-reject return (admin)")
    public ResponseEntity<ApiResponse<ReturnResult>> reject(
            @PathVariable String returnId,
            @Valid @RequestBody AdminRejectReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                returnService.adminReject(returnId, request.reason()),
                "Return rejected by admin"));
    }

    @PostMapping("/{returnId}/item-received")
    @Operation(summary = "Force-confirm return item received (admin)")
    public ResponseEntity<ApiResponse<ReturnResult>> confirmReceived(
            @PathVariable String returnId,
            @Valid @RequestBody AdminConfirmReturnReceivedRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                returnService.adminConfirmItemReceived(returnId, request.itemCondition()),
                "Return item received recorded by admin"));
    }
}
