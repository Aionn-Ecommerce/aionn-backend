package com.aionn.ordering.adapter.rest.controller;

import com.aionn.ordering.adapter.rest.dto.returns.ApproveReturnRequest;
import com.aionn.ordering.adapter.rest.dto.returns.ConfirmItemReceivedRequest;
import com.aionn.ordering.adapter.rest.dto.returns.RejectReturnRequest;
import com.aionn.ordering.adapter.rest.dto.returns.RequestReturnRequest;
import com.aionn.ordering.adapter.rest.support.session.CurrentUserId;
import com.aionn.ordering.application.dto.returns.command.ApproveReturnCommand;
import com.aionn.ordering.application.dto.returns.command.ConfirmItemReceivedCommand;
import com.aionn.ordering.application.dto.returns.command.RejectReturnCommand;
import com.aionn.ordering.application.dto.returns.command.RequestReturnCommand;
import com.aionn.ordering.application.dto.returns.result.ReturnResult;
import com.aionn.ordering.application.service.OrderReturnService;
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

@RestController
@RequestMapping("/api/v1/ordering/returns")
@RequiredArgsConstructor
@Tag(name = "Ordering - Return", description = "Order return / refund flow")
public class OrderReturnController {

    private final OrderReturnService returnService;

    @PostMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Request return")
    public ResponseEntity<ApiResponse<ReturnResult>> request(
            @CurrentUserId String userId,
            @PathVariable String orderId,
            @Valid @RequestBody RequestReturnRequest request) {
        ReturnResult result = returnService.requestReturn(new RequestReturnCommand(
                orderId, userId, request.reason(), request.evidenceUrl()));
        return ApiResponse.createdResponse("Return requested", result);
    }

    @PostMapping("/{returnId}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Merchant approve return")
    public ResponseEntity<ApiResponse<ReturnResult>> approve(
            @CurrentUserId String ownerId,
            @PathVariable String returnId,
            @Valid @RequestBody ApproveReturnRequest request) {
        ReturnResult result = returnService.approve(new ApproveReturnCommand(
                returnId, ownerId,
                request.refundAmount(), request.currency(), request.returnWarehouseId()));
        return ResponseEntity.ok(ApiResponse.success(result, "Return approved"));
    }

    @PostMapping("/{returnId}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Merchant reject return")
    public ResponseEntity<ApiResponse<ReturnResult>> reject(
            @CurrentUserId String ownerId,
            @PathVariable String returnId,
            @Valid @RequestBody RejectReturnRequest request) {
        ReturnResult result = returnService.reject(new RejectReturnCommand(
                returnId, ownerId, request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Return rejected"));
    }

    @PostMapping("/{returnId}/item-received")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Merchant confirms item received")
    public ResponseEntity<ApiResponse<ReturnResult>> confirmReceived(
            @CurrentUserId String ownerId,
            @PathVariable String returnId,
            @Valid @RequestBody ConfirmItemReceivedRequest request) {
        ReturnResult result = returnService.confirmItemReceived(new ConfirmItemReceivedCommand(
                returnId, ownerId, request.itemCondition()));
        return ResponseEntity.ok(ApiResponse.success(result, "Return item received"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List return requests by the authenticated user")
    public ResponseEntity<ApiResponse<java.util.List<ReturnResult>>> listMine(
            @CurrentUserId String userId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                returnService.listMine(userId, limit), "Returns fetched"));
    }

    @GetMapping("/merchant")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List return requests for the merchant")
    public ResponseEntity<ApiResponse<java.util.List<ReturnResult>>> listMerchant(
            @CurrentUserId String ownerId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                returnService.listMerchant(ownerId, limit), "Returns fetched"));
    }

    @GetMapping("/{returnId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get return")
    public ResponseEntity<ApiResponse<ReturnResult>> get(
            @CurrentUserId String userId,
            @PathVariable String returnId) {
        return ResponseEntity.ok(ApiResponse.success(
                returnService.getForRequester(returnId, userId), "Return fetched"));
    }
}
