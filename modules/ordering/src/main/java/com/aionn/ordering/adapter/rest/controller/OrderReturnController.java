package com.aionn.ordering.adapter.rest.controller;

import com.aionn.ordering.adapter.rest.dto.returns.ApproveReturnRequest;
import com.aionn.ordering.adapter.rest.dto.returns.ConfirmItemReceivedRequest;
import com.aionn.ordering.adapter.rest.dto.returns.RejectReturnRequest;
import com.aionn.ordering.adapter.rest.dto.returns.RequestReturnRequest;
import com.aionn.ordering.application.dto.returns.command.ReturnCommands;
import com.aionn.ordering.application.dto.returns.result.ReturnResult;
import com.aionn.ordering.application.service.OrderReturnService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ordering/returns")
@RequiredArgsConstructor
@Tag(name = "Ordering - Return", description = "Order return / refund flow")
public class OrderReturnController {

    private final OrderReturnService returnService;

    @PostMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Request return", description = "UC5.15")
    public ResponseEntity<ApiResponse<ReturnResult>> request(
            Authentication authentication,
            @PathVariable String orderId,
            @Valid @RequestBody RequestReturnRequest request) {
        ReturnResult result = returnService.requestReturn(new ReturnCommands.RequestReturn(
                orderId, authentication.getName(), request.reason(), request.evidenceUrl()));
        return ApiResponse.createdResponse("Return requested", result);
    }

    @PostMapping("/{returnId}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Merchant approve return", description = "UC5.16")
    public ResponseEntity<ApiResponse<ReturnResult>> approve(
            Authentication authentication,
            @PathVariable String returnId,
            @Valid @RequestBody ApproveReturnRequest request) {
        ReturnResult result = returnService.approve(new ReturnCommands.ApproveReturn(
                returnId, authentication.getName(),
                request.refundAmount(), request.currency(), request.returnWarehouseId()));
        return ResponseEntity.ok(ApiResponse.success(result, "Return approved"));
    }

    @PostMapping("/{returnId}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Merchant reject return", description = "UC5.17")
    public ResponseEntity<ApiResponse<ReturnResult>> reject(
            Authentication authentication,
            @PathVariable String returnId,
            @Valid @RequestBody RejectReturnRequest request) {
        ReturnResult result = returnService.reject(new ReturnCommands.RejectReturn(
                returnId, authentication.getName(), request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Return rejected"));
    }

    @PostMapping("/{returnId}/item-received")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Merchant confirms item received", description = "UC5.18")
    public ResponseEntity<ApiResponse<ReturnResult>> confirmReceived(
            Authentication authentication,
            @PathVariable String returnId,
            @Valid @RequestBody ConfirmItemReceivedRequest request) {
        ReturnResult result = returnService.confirmItemReceived(new ReturnCommands.ConfirmItemReceived(
                returnId, authentication.getName(), request.itemCondition()));
        return ResponseEntity.ok(ApiResponse.success(result, "Return item received"));
    }

    @GetMapping("/{returnId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get return")
    public ResponseEntity<ApiResponse<ReturnResult>> get(
            Authentication authentication,
            @PathVariable String returnId) {
        return ResponseEntity.ok(ApiResponse.success(
                returnService.getForRequester(returnId, authentication.getName()), "Return fetched"));
    }
}
