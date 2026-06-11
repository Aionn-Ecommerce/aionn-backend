package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.brand.CreateBrandRequest;
import com.aionn.catalog.adapter.rest.dto.brand.DeleteBrandRequest;
import com.aionn.catalog.adapter.rest.dto.brand.UpdateBrandRequest;
import com.aionn.catalog.application.dto.brand.command.CreateBrandCommand;
import com.aionn.catalog.application.dto.brand.command.DeleteBrandCommand;
import com.aionn.catalog.application.dto.brand.command.UpdateBrandCommand;
import com.aionn.catalog.application.dto.brand.result.BrandResult;
import com.aionn.catalog.application.service.BrandService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalog/brands")
@RequiredArgsConstructor
@Tag(name = "Catalog - Brand", description = "Brand management")
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Create brand")
    public ResponseEntity<ApiResponse<BrandResult>> create(@Valid @RequestBody CreateBrandRequest request) {
        BrandResult result = brandService.create(
                new CreateBrandCommand(request.name(), request.logoUrl(), request.description()));
        return ApiResponse.createdResponse("Brand created", result);
    }

    @PutMapping("/{brandId}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Update brand")
    public ResponseEntity<ApiResponse<BrandResult>> update(
            @PathVariable String brandId,
            @Valid @RequestBody UpdateBrandRequest request) {
        BrandResult result = brandService.update(new UpdateBrandCommand(
                brandId, request.name(), request.logoUrl(), request.description()));
        return ResponseEntity.ok(ApiResponse.success(result, "Brand updated"));
    }

    @PostMapping("/{brandId}/delete")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Delete brand", description = "Soft delete only")
    public ResponseEntity<Void> delete(
            @PathVariable String brandId,
            @Valid @RequestBody DeleteBrandRequest request) {
        brandService.delete(new DeleteBrandCommand(brandId, request.reason()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{brandId}")
    @Operation(summary = "Get brand", description = "Public read")
    public ResponseEntity<ApiResponse<BrandResult>> get(@PathVariable String brandId) {
        return ResponseEntity.ok(ApiResponse.success(brandService.get(brandId), "Brand fetched"));
    }
}
