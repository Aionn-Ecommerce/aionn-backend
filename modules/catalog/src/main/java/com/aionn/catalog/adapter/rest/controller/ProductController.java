package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.product.AssignBrandRequest;
import com.aionn.catalog.adapter.rest.dto.product.AssignCategoriesRequest;
import com.aionn.catalog.adapter.rest.dto.product.AssignCollectionsRequest;
import com.aionn.catalog.adapter.rest.dto.product.BulkPriceUpdateRequest;
import com.aionn.catalog.adapter.rest.dto.product.ChangeVariantPriceRequest;
import com.aionn.catalog.adapter.rest.dto.product.CreateProductRequest;
import com.aionn.catalog.adapter.rest.dto.product.DeactivateProductRequest;
import com.aionn.catalog.adapter.rest.dto.product.DefineAttributesRequest;
import com.aionn.catalog.adapter.rest.dto.product.DefineVariantRequest;
import com.aionn.catalog.adapter.rest.dto.product.EmergencyTakedownRequest;
import com.aionn.catalog.adapter.rest.dto.product.RejectProductRequest;
import com.aionn.catalog.adapter.rest.dto.product.UpdateAiMetadataRequest;
import com.aionn.catalog.adapter.rest.dto.product.UpdateMediaRequest;
import com.aionn.catalog.application.dto.product.command.ProductCommands;
import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.service.ProductService;
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
@RequestMapping("/api/v1/catalog/products")
@RequiredArgsConstructor
@Tag(name = "Catalog - Product", description = "Catalog module: product, variant, lifecycle and AI metadata")
public class ProductController {

    private final ProductService productService;

    // ===== UC3.13 / UC3.22 =====

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create product", description = "UC3.13 - merchant creates product in DRAFT")
    public ResponseEntity<ApiResponse<ProductResult>> create(
            Authentication authentication,
            @Valid @RequestBody CreateProductRequest request) {
        ProductResult result = productService.create(
                new ProductCommands.CreateProduct(request.merchantId(), request.name()));
        return ApiResponse.createdResponse("Product created", result);
    }

    @PostMapping("/{productId}/clone")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clone product", description = "UC3.22 - copy product without SKU/media")
    public ResponseEntity<ApiResponse<ProductResult>> clone(
            Authentication authentication,
            @PathVariable String productId) {
        ProductResult result = productService.clone(
                new ProductCommands.Clone(productId, authentication.getName()));
        return ApiResponse.createdResponse("Product cloned", result);
    }

    // ===== UC3.14 / UC3.23 =====

    @PostMapping("/{productId}/variants")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Define variant", description = "UC3.14 - add SKU with attribute combo and price")
    public ResponseEntity<ApiResponse<ProductResult>> defineVariant(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody DefineVariantRequest request) {
        ProductResult result = productService.defineVariant(new ProductCommands.DefineVariant(
                productId, authentication.getName(), request.attributeValues(),
                request.price(), request.currency()));
        return ApiResponse.createdResponse("Variant defined", result);
    }

    @DeleteMapping("/{productId}/variants/{skuId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove variant", description = "UC3.23 - delete a SKU; integration event for inventory")
    public ResponseEntity<ApiResponse<ProductResult>> removeVariant(
            Authentication authentication,
            @PathVariable String productId,
            @PathVariable String skuId) {
        ProductResult result = productService.removeVariant(new ProductCommands.RemoveVariant(
                productId, authentication.getName(), skuId));
        return ResponseEntity.ok(ApiResponse.success(result, "Variant removed"));
    }

    // ===== UC3.15 =====

    @PutMapping("/{productId}/media")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update media", description = "UC3.15 - replace product image list")
    public ResponseEntity<ApiResponse<ProductResult>> updateMedia(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody UpdateMediaRequest request) {
        ProductResult result = productService.updateMedia(new ProductCommands.UpdateMedia(
                productId, authentication.getName(), request.imageList()));
        return ResponseEntity.ok(ApiResponse.success(result, "Media updated"));
    }

    // ===== UC3.16 =====

    @PutMapping("/{productId}/brand")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Assign brand", description = "UC3.16 - bind to an approved brand")
    public ResponseEntity<ApiResponse<ProductResult>> assignBrand(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody AssignBrandRequest request) {
        ProductResult result = productService.assignBrand(new ProductCommands.AssignBrand(
                productId, authentication.getName(), request.brandId()));
        return ResponseEntity.ok(ApiResponse.success(result, "Brand assigned"));
    }

    // ===== UC3.17 =====

    @PutMapping("/{productId}/categories")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Assign categories", description = "UC3.17 - assign one or more categories")
    public ResponseEntity<ApiResponse<ProductResult>> categorize(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody AssignCategoriesRequest request) {
        ProductResult result = productService.categorize(new ProductCommands.AssignCategories(
                productId, authentication.getName(), request.categoryIds()));
        return ResponseEntity.ok(ApiResponse.success(result, "Categories assigned"));
    }

    // ===== UC3.18 / UC3.19 =====

    @PostMapping("/{productId}/publish")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Publish product", description = "UC3.18 - admin approves a product for sale")
    public ResponseEntity<ApiResponse<ProductResult>> publish(
            Authentication authentication,
            @PathVariable String productId) {
        ProductResult result = productService.publish(
                new ProductCommands.Publish(productId, authentication.getName()));
        return ResponseEntity.ok(ApiResponse.success(result, "Product published"));
    }

    @PostMapping("/{productId}/reject")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Reject product", description = "UC3.19 - admin rejects with reason code")
    public ResponseEntity<ApiResponse<ProductResult>> reject(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody RejectProductRequest request) {
        ProductResult result = productService.reject(new ProductCommands.Reject(
                productId, authentication.getName(), request.reasonCode(), request.feedback()));
        return ResponseEntity.ok(ApiResponse.success(result, "Product rejected"));
    }

    // ===== UC3.20 / UC3.21 =====

    @PostMapping("/{productId}/deactivate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Deactivate (hide)", description = "UC3.20 - merchant hides a product from sale")
    public ResponseEntity<ApiResponse<ProductResult>> deactivate(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody DeactivateProductRequest request) {
        ProductResult result = productService.deactivate(new ProductCommands.Deactivate(
                productId, authentication.getName(), request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Product deactivated"));
    }

    @PostMapping("/{productId}/restore")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Restore product", description = "UC3.21 - merchant restores a hidden product")
    public ResponseEntity<ApiResponse<ProductResult>> restore(
            Authentication authentication,
            @PathVariable String productId) {
        ProductResult result = productService.restore(
                new ProductCommands.Restore(productId, authentication.getName()));
        return ResponseEntity.ok(ApiResponse.success(result, "Product restored"));
    }

    // ===== UC3.24 =====

    @PutMapping("/{productId}/variants/{skuId}/price")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change variant price", description = "UC3.24 - update price for a SKU")
    public ResponseEntity<ApiResponse<ProductResult>> changeVariantPrice(
            Authentication authentication,
            @PathVariable String productId,
            @PathVariable String skuId,
            @Valid @RequestBody ChangeVariantPriceRequest request) {
        ProductResult result = productService.changeVariantPrice(new ProductCommands.ChangeVariantPrice(
                productId, authentication.getName(), skuId, request.newPrice(), request.currency()));
        return ResponseEntity.ok(ApiResponse.success(result, "Price updated"));
    }

    // ===== UC3.25 =====

    @PutMapping("/{productId}/ai-metadata")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update AI metadata", description = "UC3.25 - tags + hidden description for the AI agent")
    public ResponseEntity<ApiResponse<ProductResult>> updateAiMetadata(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody UpdateAiMetadataRequest request) {
        ProductResult result = productService.updateAiMetadata(new ProductCommands.UpdateAiMetadata(
                productId, authentication.getName(), request.tags(), request.aiDescription()));
        return ResponseEntity.ok(ApiResponse.success(result, "AI metadata updated"));
    }

    // ===== UC3.26 =====

    @PutMapping("/{productId}/collections")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Assign collections", description = "UC3.26 - bind product to merchandising collections")
    public ResponseEntity<ApiResponse<ProductResult>> assignCollections(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody AssignCollectionsRequest request) {
        ProductResult result = productService.assignCollections(new ProductCommands.AssignCollections(
                productId, authentication.getName(), request.collectionIds()));
        return ResponseEntity.ok(ApiResponse.success(result, "Collections assigned"));
    }

    // ===== UC3.27 / UC3.29 =====

    @PostMapping("/bulk-price")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Bulk price update", description = "UC3.27/UC3.29 - apply a price change across many SKUs")
    public ResponseEntity<ApiResponse<Void>> bulkPriceUpdate(
            Authentication authentication,
            @Valid @RequestBody BulkPriceUpdateRequest request) {
        productService.bulkPriceUpdate(new ProductCommands.BulkPriceUpdate(
                authentication.getName(), request.skuIds(), request.changeType(),
                request.value(), request.currency()));
        return ResponseEntity.accepted().body(ApiResponse.success("Bulk price update accepted"));
    }

    // ===== UC3.28 =====

    @PostMapping("/{productId}/takedown")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Emergency takedown", description = "UC3.28 - admin removes product immediately")
    public ResponseEntity<ApiResponse<ProductResult>> emergencyTakedown(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody EmergencyTakedownRequest request) {
        ProductResult result = productService.emergencyTakedown(new ProductCommands.EmergencyTakedown(
                productId, authentication.getName(), request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Product taken down"));
    }

    // ===== UC3.32 =====

    @PutMapping("/{productId}/attributes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Define attributes", description = "UC3.32 - apply category-template attributes to product")
    public ResponseEntity<ApiResponse<ProductResult>> defineAttributes(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody DefineAttributesRequest request) {
        ProductResult result = productService.defineAttributes(new ProductCommands.DefineAttributes(
                productId, authentication.getName(), request.attributes()));
        return ResponseEntity.ok(ApiResponse.success(result, "Attributes applied"));
    }

    // ===== Read =====

    @GetMapping("/{productId}")
    @Operation(summary = "Get product", description = "Public read of a product")
    public ResponseEntity<ApiResponse<ProductResult>> get(@PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.get(productId), "Product fetched"));
    }
}

