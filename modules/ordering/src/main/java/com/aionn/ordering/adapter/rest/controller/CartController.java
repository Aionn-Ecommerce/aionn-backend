package com.aionn.ordering.adapter.rest.controller;

import com.aionn.ordering.adapter.rest.dto.cart.AddCartItemRequest;
import com.aionn.ordering.adapter.rest.dto.cart.ApplyVoucherRequest;
import com.aionn.ordering.adapter.rest.dto.cart.UpdateCartItemRequest;
import com.aionn.ordering.application.dto.cart.command.AddItemCommand;
import com.aionn.ordering.application.dto.cart.command.ApplyVoucherCommand;
import com.aionn.ordering.application.dto.cart.command.ClearCartCommand;
import com.aionn.ordering.application.dto.cart.command.RemoveItemCommand;
import com.aionn.ordering.application.dto.cart.command.RemoveVoucherCommand;
import com.aionn.ordering.application.dto.cart.command.UpdateItemQtyCommand;
import com.aionn.ordering.application.dto.cart.result.CartResult;
import com.aionn.ordering.application.service.CartService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ordering/cart")
@RequiredArgsConstructor
@Tag(name = "Ordering - Cart", description = "User shopping cart endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my cart")
    public ResponseEntity<ApiResponse<CartResult>> getMyCart(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getMyCart(authentication.getName()),
                "Cart fetched"));
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add item")
    public ResponseEntity<ApiResponse<CartResult>> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request) {
        CartResult result = cartService.addItem(new AddItemCommand(
                authentication.getName(), request.skuId(), request.qty()));
        return ResponseEntity.ok(ApiResponse.success(result, "Item added"));
    }

    @PutMapping("/items/{skuId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update item qty")
    public ResponseEntity<ApiResponse<CartResult>> updateItem(
            Authentication authentication,
            @PathVariable String skuId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResult result = cartService.updateItemQty(new UpdateItemQtyCommand(
                authentication.getName(), skuId, request.newQty()));
        return ResponseEntity.ok(ApiResponse.success(result, "Item updated"));
    }

    @DeleteMapping("/items/{skuId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove item")
    public ResponseEntity<ApiResponse<CartResult>> removeItem(
            Authentication authentication,
            @PathVariable String skuId) {
        CartResult result = cartService.removeItem(new RemoveItemCommand(authentication.getName(), skuId));
        return ResponseEntity.ok(ApiResponse.success(result, "Item removed"));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<CartResult>> clearCart(Authentication authentication) {
        CartResult result = cartService.clearCart(new ClearCartCommand(authentication.getName(), "user-cleared"));
        return ResponseEntity.ok(ApiResponse.success(result, "Cart cleared"));
    }

    @PostMapping("/voucher")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Apply voucher")
    public ResponseEntity<ApiResponse<CartResult>> applyVoucher(
            Authentication authentication,
            @Valid @RequestBody ApplyVoucherRequest request) {
        CartResult result = cartService.applyVoucher(new ApplyVoucherCommand(
                authentication.getName(), request.voucherCode()));
        return ResponseEntity.ok(ApiResponse.success(result, "Voucher applied"));
    }

    @DeleteMapping("/voucher")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove voucher")
    public ResponseEntity<ApiResponse<CartResult>> removeVoucher(Authentication authentication) {
        CartResult result = cartService.removeVoucher(new RemoveVoucherCommand(authentication.getName()));
        return ResponseEntity.ok(ApiResponse.success(result, "Voucher removed"));
    }
}
