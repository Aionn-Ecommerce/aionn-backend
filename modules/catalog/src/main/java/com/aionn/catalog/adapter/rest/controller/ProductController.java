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
import com.aionn.catalog.application.dto.product.command.AssignBrandCommand;
import com.aionn.catalog.application.dto.product.command.AssignCategoriesCommand;
import com.aionn.catalog.application.dto.product.command.AssignCollectionsCommand;
import com.aionn.catalog.application.dto.product.command.BulkPriceUpdateCommand;
import com.aionn.catalog.application.dto.product.command.ChangeVariantPriceCommand;
import com.aionn.catalog.application.dto.product.command.CloneCommand;
import com.aionn.catalog.application.dto.product.command.CreateProductCommand;
import com.aionn.catalog.application.dto.product.command.DeactivateCommand;
import com.aionn.catalog.application.dto.product.command.DefineAttributesCommand;
import com.aionn.catalog.application.dto.product.command.DefineVariantCommand;
import com.aionn.catalog.application.dto.product.command.EmergencyTakedownCommand;
import com.aionn.catalog.application.dto.product.command.PublishCommand;
import com.aionn.catalog.application.dto.product.command.RejectCommand;
import com.aionn.catalog.application.dto.product.command.RemoveVariantCommand;
import com.aionn.catalog.application.dto.product.command.RestoreCommand;
import com.aionn.catalog.application.dto.product.command.UpdateAiMetadataCommand;
import com.aionn.catalog.application.dto.product.command.UpdateMediaCommand;
import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.dto.search.ProductSearchCriteria;
import com.aionn.catalog.application.dto.search.ProductSearchResult;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aionn.catalog.application.dto.common.PageResult;
import com.aionn.catalog.domain.valueobject.ProductStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalog/products")
@RequiredArgsConstructor
@Tag(name = "Catalog - Product", description = "Product, variant, lifecycle and AI metadata")
public class ProductController {

        private final ProductService productService;

        @PostMapping
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Create product", description = "Merchant creates a product in DRAFT")
        public ResponseEntity<ApiResponse<ProductResult>> create(
                        Authentication authentication,
                        @Valid @RequestBody CreateProductRequest request) {
                ProductResult result = productService.create(
                                new CreateProductCommand(authentication.getName(), request.name()));
                return ApiResponse.createdResponse("Product created", result);
        }

        @PostMapping("/{productId}/clone")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Clone product", description = "Copy product without SKUs / media")
        public ResponseEntity<ApiResponse<ProductResult>> clone(
                        Authentication authentication,
                        @PathVariable String productId) {
                ProductResult result = productService.clone(
                                new CloneCommand(productId, authentication.getName()));
                return ApiResponse.createdResponse("Product cloned", result);
        }

        @PostMapping("/{productId}/variants")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Define variant", description = "Add a SKU with attribute combo and price")
        public ResponseEntity<ApiResponse<ProductResult>> defineVariant(
                        Authentication authentication,
                        @PathVariable String productId,
                        @Valid @RequestBody DefineVariantRequest request) {
                ProductResult result = productService.defineVariant(new DefineVariantCommand(
                                productId, authentication.getName(), request.attributeValues(),
                                request.price(), request.currency()));
                return ApiResponse.createdResponse("Variant defined", result);
        }

        @DeleteMapping("/{productId}/variants/{skuId}")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Remove variant", description = "Delete a SKU; emits an integration event for inventory")
        public ResponseEntity<ApiResponse<ProductResult>> removeVariant(
                        Authentication authentication,
                        @PathVariable String productId,
                        @PathVariable String skuId) {
                ProductResult result = productService.removeVariant(new RemoveVariantCommand(
                                productId, authentication.getName(), skuId));
                return ResponseEntity.ok(ApiResponse.success(result, "Variant removed"));
        }

        @PutMapping("/{productId}/media")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Update media", description = "Replace product image list")
        public ResponseEntity<ApiResponse<ProductResult>> updateMedia(
                        Authentication authentication,
                        @PathVariable String productId,
                        @Valid @RequestBody UpdateMediaRequest request) {
                ProductResult result = productService.updateMedia(new UpdateMediaCommand(
                                productId, authentication.getName(), request.imageList()));
                return ResponseEntity.ok(ApiResponse.success(result, "Media updated"));
        }

        @PutMapping("/{productId}/brand")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Assign brand", description = "Bind to an approved brand")
        public ResponseEntity<ApiResponse<ProductResult>> assignBrand(
                        Authentication authentication,
                        @PathVariable String productId,
                        @Valid @RequestBody AssignBrandRequest request) {
                ProductResult result = productService.assignBrand(new AssignBrandCommand(
                                productId, authentication.getName(), request.brandId()));
                return ResponseEntity.ok(ApiResponse.success(result, "Brand assigned"));
        }

        @PutMapping("/{productId}/categories")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Assign categories", description = "Assign one or more categories")
        public ResponseEntity<ApiResponse<ProductResult>> categorize(
                        Authentication authentication,
                        @PathVariable String productId,
                        @Valid @RequestBody AssignCategoriesRequest request) {
                ProductResult result = productService.categorize(new AssignCategoriesCommand(
                                productId, authentication.getName(), request.categoryIds()));
                return ResponseEntity.ok(ApiResponse.success(result, "Categories assigned"));
        }

        @PostMapping("/{productId}/publish")
        @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
        @Operation(summary = "Publish product", description = "Admin approves a product for sale")
        public ResponseEntity<ApiResponse<ProductResult>> publish(
                        Authentication authentication,
                        @PathVariable String productId) {
                ProductResult result = productService.publish(
                                new PublishCommand(productId, authentication.getName()));
                return ResponseEntity.ok(ApiResponse.success(result, "Product published"));
        }

        @PostMapping("/{productId}/reject")
        @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
        @Operation(summary = "Reject product", description = "Admin rejects with a reason code")
        public ResponseEntity<ApiResponse<ProductResult>> reject(
                        Authentication authentication,
                        @PathVariable String productId,
                        @Valid @RequestBody RejectProductRequest request) {
                ProductResult result = productService.reject(new RejectCommand(
                                productId, authentication.getName(), request.reasonCode(), request.feedback()));
                return ResponseEntity.ok(ApiResponse.success(result, "Product rejected"));
        }

        @PostMapping("/{productId}/deactivate")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Deactivate (hide)", description = "Merchant hides a product from sale")
        public ResponseEntity<ApiResponse<ProductResult>> deactivate(
                        Authentication authentication,
                        @PathVariable String productId,
                        @Valid @RequestBody DeactivateProductRequest request) {
                ProductResult result = productService.deactivate(new DeactivateCommand(
                                productId, authentication.getName(), request.reason()));
                return ResponseEntity.ok(ApiResponse.success(result, "Product deactivated"));
        }

        @PostMapping("/{productId}/restore")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Restore product", description = "Merchant restores a hidden product")
        public ResponseEntity<ApiResponse<ProductResult>> restore(
                        Authentication authentication,
                        @PathVariable String productId) {
                ProductResult result = productService.restore(
                                new RestoreCommand(productId, authentication.getName()));
                return ResponseEntity.ok(ApiResponse.success(result, "Product restored"));
        }

        @PutMapping("/{productId}/variants/{skuId}/price")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Change variant price", description = "Update price for a single SKU")
        public ResponseEntity<ApiResponse<ProductResult>> changeVariantPrice(
                        Authentication authentication,
                        @PathVariable String productId,
                        @PathVariable String skuId,
                        @Valid @RequestBody ChangeVariantPriceRequest request) {
                ProductResult result = productService.changeVariantPrice(new ChangeVariantPriceCommand(
                                productId, authentication.getName(), skuId, request.newPrice(), request.currency()));
                return ResponseEntity.ok(ApiResponse.success(result, "Price updated"));
        }

        @PutMapping("/{productId}/ai-metadata")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Update AI metadata", description = "Tags + hidden description for the AI agent")
        public ResponseEntity<ApiResponse<ProductResult>> updateAiMetadata(
                        Authentication authentication,
                        @PathVariable String productId,
                        @Valid @RequestBody UpdateAiMetadataRequest request) {
                ProductResult result = productService.updateAiMetadata(new UpdateAiMetadataCommand(
                                productId, authentication.getName(), request.tags(), request.aiDescription()));
                return ResponseEntity.ok(ApiResponse.success(result, "AI metadata updated"));
        }

        @PutMapping("/{productId}/collections")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Assign collections", description = "Bind product to merchandising collections")
        public ResponseEntity<ApiResponse<ProductResult>> assignCollections(
                        Authentication authentication,
                        @PathVariable String productId,
                        @Valid @RequestBody AssignCollectionsRequest request) {
                ProductResult result = productService.assignCollections(new AssignCollectionsCommand(
                                productId, authentication.getName(), request.collectionIds()));
                return ResponseEntity.ok(ApiResponse.success(result, "Collections assigned"));
        }

        @PostMapping("/bulk-price")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Bulk price update", description = "Apply a price change across many SKUs")
        public ResponseEntity<ApiResponse<Void>> bulkPriceUpdate(
                        Authentication authentication,
                        @Valid @RequestBody BulkPriceUpdateRequest request) {
                productService.bulkPriceUpdate(new BulkPriceUpdateCommand(
                                authentication.getName(), request.skuIds(), request.changeType(),
                                request.value(), request.currency()));
                return ResponseEntity.accepted().body(ApiResponse.success("Bulk price update accepted"));
        }

        @PostMapping("/{productId}/takedown")
        @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
        @Operation(summary = "Emergency takedown", description = "Admin removes product immediately")
        public ResponseEntity<ApiResponse<ProductResult>> emergencyTakedown(
                        Authentication authentication,
                        @PathVariable String productId,
                        @Valid @RequestBody EmergencyTakedownRequest request) {
                ProductResult result = productService.emergencyTakedown(new EmergencyTakedownCommand(
                                productId, authentication.getName(), request.reason()));
                return ResponseEntity.ok(ApiResponse.success(result, "Product taken down"));
        }

        @PutMapping("/{productId}/attributes")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Define attributes", description = "Apply category-template attributes to product")
        public ResponseEntity<ApiResponse<ProductResult>> defineAttributes(
                        Authentication authentication,
                        @PathVariable String productId,
                        @Valid @RequestBody DefineAttributesRequest request) {
                ProductResult result = productService.defineAttributes(new DefineAttributesCommand(
                                productId, authentication.getName(), request.attributes()));
                return ResponseEntity.ok(ApiResponse.success(result, "Attributes applied"));
        }

        @GetMapping("/{productId}")
        @Operation(summary = "Get product", description = "Public read of a product")
        public ResponseEntity<ApiResponse<ProductResult>> get(@PathVariable String productId) {
                return ResponseEntity.ok(ApiResponse.success(productService.get(productId), "Product fetched"));
        }

        @GetMapping
        @Operation(summary = "List products by merchant", description = "Public read - paginated list by merchantId")
        public ResponseEntity<ApiResponse<PageResult<ProductResult>>> listByMerchant(
                        @RequestParam String merchantId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                return ResponseEntity.ok(ApiResponse.success(
                        productService.listByMerchant(merchantId, page, size), "Products fetched"));
        }

        @GetMapping("/search")
        @Operation(summary = "Search products",
                description = "Public faceted search. Supports free-text q, brand/category multi-select, "
                        + "price range, attribute facets (attr.<key>=value), and sort.")
        public ResponseEntity<ApiResponse<ProductSearchResult>> search(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String merchantId,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) List<String> categoryIds,
                        @RequestParam(required = false) List<String> brandIds,
                        @RequestParam(required = false) BigDecimal priceMin,
                        @RequestParam(required = false) BigDecimal priceMax,
                        @RequestParam(required = false) String sort,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam Map<String, String> allParams) {
                ProductStatus productStatus = status != null ? ProductStatus.valueOf(status) : null;
                ProductSearchCriteria.Sort sortEnum;
                try {
                        sortEnum = sort == null ? ProductSearchCriteria.Sort.RELEVANCE
                                : ProductSearchCriteria.Sort.valueOf(sort.toUpperCase());
                } catch (IllegalArgumentException ex) {
                        sortEnum = ProductSearchCriteria.Sort.RELEVANCE;
                }
                Map<String, List<String>> attributes = new LinkedHashMap<>();
                for (Map.Entry<String, String> e : allParams.entrySet()) {
                        if (e.getKey().startsWith("attr.") && e.getValue() != null && !e.getValue().isBlank()) {
                                String key = e.getKey().substring(5);
                                attributes.put(key, Arrays.stream(e.getValue().split(","))
                                                .map(String::trim)
                                                .filter(s -> !s.isEmpty())
                                                .toList());
                        }
                }
                ProductSearchCriteria criteria = new ProductSearchCriteria(
                                q, merchantId, productStatus,
                                categoryIds == null ? List.of() : categoryIds,
                                brandIds == null ? List.of() : brandIds,
                                priceMin, priceMax,
                                attributes, sortEnum, page, size);
                return ResponseEntity.ok(ApiResponse.success(
                                productService.search(criteria), "Search results"));
        }

        @GetMapping("/{productId}/recommendations")
        @Operation(summary = "Get related products", description = "Public read of related products based on category/brand")
        public ResponseEntity<ApiResponse<List<ProductResult>>> getRelatedProducts(
                @PathVariable String productId,
                @RequestParam(defaultValue = "5") int limit) {
            List<ProductResult> results = productService.getRelatedProducts(productId, limit);
            return ResponseEntity.ok(ApiResponse.success(results, "Related products fetched"));
        }

        @GetMapping("/recommendations/popular")
        @Operation(summary = "Get popular products", description = "Public read of popular products based on ratings")
        public ResponseEntity<ApiResponse<List<ProductResult>>> getPopularProducts(
                @RequestParam(defaultValue = "5") int limit) {
            List<ProductResult> results = productService.getPopularProducts(limit);
            return ResponseEntity.ok(ApiResponse.success(results, "Popular products fetched"));
        }

        @GetMapping("/recommendations/personalized")
        @Operation(summary = "Get personalized products", description = "Public read of personalized products based on provided categories/brands or database history")
        public ResponseEntity<ApiResponse<List<ProductResult>>> getPersonalizedProducts(
                Authentication authentication,
                @RequestParam(required = false) List<String> categoryIds,
                @RequestParam(required = false) List<String> brandIds,
                @RequestParam(defaultValue = "5") int limit) {
            String userId = authentication != null ? authentication.getName() : null;
            List<ProductResult> results = productService.getPersonalizedProducts(userId, categoryIds, brandIds, limit);
            return ResponseEntity.ok(ApiResponse.success(results, "Personalized products fetched"));
        }

        @PostMapping("/{productId}/view")
        @Operation(summary = "Track product view", description = "Track product view in the database for personalized recommendations")
        public ResponseEntity<ApiResponse<Void>> trackView(
                Authentication authentication,
                @PathVariable String productId) {
            if (authentication != null) {
                productService.trackProductView(productId, authentication.getName());
            }
            return ResponseEntity.ok(ApiResponse.success(null, "Product view tracked"));
        }
}
