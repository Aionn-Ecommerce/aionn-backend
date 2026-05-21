package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.category.CreateCategoryRequest;
import com.aionn.catalog.adapter.rest.dto.category.MoveCategoryRequest;
import com.aionn.catalog.adapter.rest.dto.category.UpdateCategoryRequest;
import com.aionn.catalog.application.dto.category.command.CreateCategoryCommand;
import com.aionn.catalog.application.dto.category.command.MoveCategoryCommand;
import com.aionn.catalog.application.dto.category.command.UpdateCategoryCommand;
import com.aionn.catalog.application.dto.category.result.CategoryResult;
import com.aionn.catalog.application.service.CategoryService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalog/categories")
@RequiredArgsConstructor
@Tag(name = "Catalog - Category", description = "Catalog module: category tree management")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Create category", description = "UC3.6")
    public ResponseEntity<ApiResponse<CategoryResult>> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResult result = categoryService.create(
                new CreateCategoryCommand(request.parentId(), request.name(), request.slug()));
        return ApiResponse.createdResponse("Category created", result);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Update category", description = "UC3.7")
    public ResponseEntity<ApiResponse<CategoryResult>> update(
            @PathVariable String categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResult result = categoryService.update(
                new UpdateCategoryCommand(categoryId, request.name(), request.iconUrl(), request.active()));
        return ResponseEntity.ok(ApiResponse.success(result, "Category updated"));
    }

    @PostMapping("/{categoryId}/move")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Move category", description = "UC3.8")
    public ResponseEntity<ApiResponse<CategoryResult>> move(
            @PathVariable String categoryId,
            @Valid @RequestBody MoveCategoryRequest request) {
        CategoryResult result = categoryService.move(new MoveCategoryCommand(categoryId, request.newParentId()));
        return ResponseEntity.ok(ApiResponse.success(result, "Category moved"));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Delete category", description = "UC3.9")
    public ResponseEntity<Void> delete(@PathVariable String categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category", description = "Public read")
    public ResponseEntity<ApiResponse<CategoryResult>> get(@PathVariable String categoryId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.get(categoryId), "Category fetched"));
    }
}

