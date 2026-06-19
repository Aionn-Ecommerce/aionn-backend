package com.aionn.ordering.adapter.rest.controller;

import com.aionn.ordering.adapter.rest.dto.order.CancelOrderRequest;
import com.aionn.ordering.adapter.rest.dto.order.ChangeShippingInfoRequest;
import com.aionn.ordering.adapter.rest.dto.order.ConfirmPreparationRequest;
import com.aionn.ordering.adapter.rest.dto.order.ConfirmShippedRequest;
import com.aionn.ordering.adapter.rest.dto.order.PlaceOrderRequest;
import com.aionn.ordering.adapter.rest.dto.order.RejectOrderRequest;
import com.aionn.ordering.adapter.rest.support.session.CurrentUserId;
import com.aionn.ordering.application.dto.order.command.CancelOrderCommand;
import com.aionn.ordering.application.dto.order.command.ChangeShippingInfoCommand;
import com.aionn.ordering.application.dto.order.command.ConfirmDeliveredCommand;
import com.aionn.ordering.application.dto.order.command.ConfirmPreparationCommand;
import com.aionn.ordering.application.dto.order.command.ConfirmShippedCommand;
import com.aionn.ordering.application.dto.order.command.PlaceOrderCommand;
import com.aionn.ordering.application.dto.order.command.RejectOrderCommand;
import com.aionn.ordering.application.dto.order.result.MerchantOrderAnalyticsResult;
import com.aionn.ordering.application.dto.order.result.OrderResult;
import com.aionn.ordering.application.service.OrderService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ordering/orders")
@RequiredArgsConstructor
@Tag(name = "Ordering - Order", description = "Order placement and lifecycle")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Place order")
    public ResponseEntity<ApiResponse<OrderResult>> place(
            @CurrentUserId String userId,
            @Valid @RequestBody PlaceOrderRequest request) {
        OrderResult result = orderService.placeOrder(new PlaceOrderCommand(
                userId,
                request.addressId(),
                request.paymentMethodId(),
                request.currency(),
                request.shippingFee(),
                request.shippingAddress(),
                request.selectedSkuIds(),
                request.gateway()));
        return ApiResponse.createdResponse("Order placed", result);
    }

    @PostMapping("/{orderId}/confirm-preparation")
    @PreAuthorize("hasAnyAuthority('ROLE_MERCHANT','ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Merchant confirms preparation")
    public ResponseEntity<ApiResponse<OrderResult>> confirmPreparation(
            @CurrentUserId String ownerId,
            @PathVariable String orderId,
            @Valid @RequestBody(required = false) ConfirmPreparationRequest request) {
        OrderResult result = orderService.confirmPreparation(
                new ConfirmPreparationCommand(orderId, ownerId));
        return ResponseEntity.ok(ApiResponse.success(result, "Preparation confirmed"));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "User cancel")
    public ResponseEntity<ApiResponse<OrderResult>> cancel(
            @CurrentUserId String userId,
            @PathVariable String orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        OrderResult result = orderService.cancel(new CancelOrderCommand(
                orderId, userId, request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Order cancelled"));
    }

    @PostMapping("/{orderId}/reject")
    @PreAuthorize("hasAnyAuthority('ROLE_MERCHANT','ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Merchant reject")
    public ResponseEntity<ApiResponse<OrderResult>> reject(
            @CurrentUserId String ownerId,
            @PathVariable String orderId,
            @Valid @RequestBody RejectOrderRequest request) {
        OrderResult result = orderService.rejectByMerchant(new RejectOrderCommand(
                orderId, ownerId, request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Order rejected"));
    }

    @PutMapping("/{orderId}/shipping-info")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change shipping info")
    public ResponseEntity<ApiResponse<OrderResult>> changeShippingInfo(
            @CurrentUserId String userId,
            @PathVariable String orderId,
            @Valid @RequestBody ChangeShippingInfoRequest request) {
        OrderResult result = orderService.changeShippingInfo(new ChangeShippingInfoCommand(
                orderId, userId, request.newAddress(), request.newShippingFee()));
        return ResponseEntity.ok(ApiResponse.success(result, "Shipping info changed"));
    }

    @PostMapping("/{orderId}/ship")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Mark shipped", description = "Triggered by shipping when the carrier collects the parcel")
    public ResponseEntity<ApiResponse<OrderResult>> ship(
            @PathVariable String orderId,
            @Valid @RequestBody ConfirmShippedRequest request) {
        OrderResult result = orderService.markShipped(new ConfirmShippedCommand(orderId, request.shipmentId()));
        return ResponseEntity.ok(ApiResponse.success(result, "Order shipped"));
    }

    @PostMapping("/{orderId}/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Complete order", description = "Shipment delivered")
    public ResponseEntity<ApiResponse<OrderResult>> complete(@PathVariable String orderId) {
        OrderResult result = orderService.complete(new ConfirmDeliveredCommand(orderId));
        return ResponseEntity.ok(ApiResponse.success(result, "Order completed"));
    }

    @GetMapping("/merchant")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    @Operation(summary = "List merchant orders")
    public ResponseEntity<ApiResponse<List<OrderResult>>> listForMerchant(
            @CurrentUserId String ownerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return ResponseEntity.ok(ApiResponse.success(
                orderService.listByMerchantOwner(ownerId, status, safeLimit), "Orders fetched"));
    }

    @GetMapping("/merchant/analytics")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    @Operation(summary = "Get merchant order analytics")
    public ResponseEntity<ApiResponse<MerchantOrderAnalyticsResult>> merchantAnalytics(
            @CurrentUserId String ownerId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getMerchantAnalytics(ownerId, from, to), "Merchant analytics fetched"));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order")
    public ResponseEntity<ApiResponse<OrderResult>> get(
            @CurrentUserId String userId,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getForRequester(orderId, userId), "Order fetched"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List my orders")
    public ResponseEntity<ApiResponse<List<OrderResult>>> listMine(
            @CurrentUserId String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return ResponseEntity.ok(ApiResponse.success(
                orderService.listByUser(userId, status, safeLimit), "Orders fetched"));
    }
}
