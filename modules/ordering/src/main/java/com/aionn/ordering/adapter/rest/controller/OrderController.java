package com.aionn.ordering.adapter.rest.controller;

import com.aionn.ordering.adapter.rest.dto.order.CancelOrderRequest;
import com.aionn.ordering.adapter.rest.dto.order.ChangeShippingInfoRequest;
import com.aionn.ordering.adapter.rest.dto.order.ConfirmPreparationRequest;
import com.aionn.ordering.adapter.rest.dto.order.ConfirmShippedRequest;
import com.aionn.ordering.adapter.rest.dto.order.PlaceOrderRequest;
import com.aionn.ordering.adapter.rest.dto.order.RejectOrderRequest;
import com.aionn.ordering.application.dto.order.command.OrderCommands;
import com.aionn.ordering.application.dto.order.result.OrderResult;
import com.aionn.ordering.application.service.OrderService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordering/orders")
@RequiredArgsConstructor
@Tag(name = "Ordering - Order", description = "Order placement and lifecycle")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Place order", description = "UC5.6")
    public ResponseEntity<ApiResponse<OrderResult>> place(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request) {
        OrderResult result = orderService.placeOrder(new OrderCommands.PlaceOrder(
                authentication.getName(),
                request.addressId(),
                request.paymentMethodId(),
                request.currency(),
                request.shippingFee(),
                request.shippingAddress()));
        return ApiResponse.createdResponse("Order placed", result);
    }

    @PostMapping("/{orderId}/confirm-preparation")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Merchant confirms preparation", description = "UC5.8")
    public ResponseEntity<ApiResponse<OrderResult>> confirmPreparation(
            Authentication authentication,
            @PathVariable String orderId,
            @Valid @RequestBody(required = false) ConfirmPreparationRequest request) {
        OrderResult result = orderService.confirmPreparation(
                new OrderCommands.ConfirmPreparation(orderId, authentication.getName()));
        return ResponseEntity.ok(ApiResponse.success(result, "Preparation confirmed"));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "User cancel", description = "UC5.9")
    public ResponseEntity<ApiResponse<OrderResult>> cancel(
            Authentication authentication,
            @PathVariable String orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        OrderResult result = orderService.cancel(new OrderCommands.CancelOrder(
                orderId, authentication.getName(), request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Order cancelled"));
    }

    @PostMapping("/{orderId}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Merchant reject", description = "UC5.13")
    public ResponseEntity<ApiResponse<OrderResult>> reject(
            Authentication authentication,
            @PathVariable String orderId,
            @Valid @RequestBody RejectOrderRequest request) {
        OrderResult result = orderService.rejectByMerchant(new OrderCommands.RejectOrder(
                orderId, authentication.getName(), request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Order rejected"));
    }

    @PutMapping("/{orderId}/shipping-info")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change shipping info", description = "UC5.14")
    public ResponseEntity<ApiResponse<OrderResult>> changeShippingInfo(
            Authentication authentication,
            @PathVariable String orderId,
            @Valid @RequestBody ChangeShippingInfoRequest request) {
        OrderResult result = orderService.changeShippingInfo(new OrderCommands.ChangeShippingInfo(
                orderId, authentication.getName(), request.newAddress(), request.newShippingFee()));
        return ResponseEntity.ok(ApiResponse.success(result, "Shipping info changed"));
    }

    @PostMapping("/{orderId}/ship")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Mark shipped", description = "Triggered by Shipping context once carrier collects")
    public ResponseEntity<ApiResponse<OrderResult>> ship(
            @PathVariable String orderId,
            @Valid @RequestBody ConfirmShippedRequest request) {
        OrderResult result = orderService.markShipped(new OrderCommands.ConfirmShipped(orderId, request.shipmentId()));
        return ResponseEntity.ok(ApiResponse.success(result, "Order shipped"));
    }

    @PostMapping("/{orderId}/complete")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Complete order", description = "UC5.11 - shipment delivered")
    public ResponseEntity<ApiResponse<OrderResult>> complete(@PathVariable String orderId) {
        OrderResult result = orderService.complete(new OrderCommands.ConfirmDelivered(orderId));
        return ResponseEntity.ok(ApiResponse.success(result, "Order completed"));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order")
    public ResponseEntity<ApiResponse<OrderResult>> get(
            Authentication authentication,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getForRequester(orderId, authentication.getName()), "Order fetched"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List my orders")
    public ResponseEntity<ApiResponse<List<OrderResult>>> listMine(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return ResponseEntity.ok(ApiResponse.success(
                orderService.listByUser(authentication.getName(), safeLimit), "Orders fetched"));
    }
}
