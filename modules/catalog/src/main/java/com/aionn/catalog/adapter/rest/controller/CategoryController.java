package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.category.CreateCategoryRequest;
import com.aionn.catalog.adapter.rest.dto.category.MoveCategoryRequest;
import com.aionn.catalog.adapter.rest.dto.category.UpdateCategoryRequest;
import com.aionn.catalog.application.dto.category.command.CreateCategoryCommand;
import com.aionn.catalog.application.dto.category.command.MoveCategoryCommand;
import com.aionn.catalog.application.dto.category.command.UpdateCategoryCommand;
import com.aionn.catalog.application.dto.category.result.CategoryResult;
import com.aionn.catalog.application.dto.category.result.CategoryTreeNode;
import com.aionn.catalog.application.service.CategoryService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog/categories")
@RequiredArgsConstructor
@Tag(name = "Catalog - Category", description = "Category tree management")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Create category")
    public ResponseEntity<ApiResponse<CategoryResult>> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResult result = categoryService.create(
                new CreateCategoryCommand(request.parentId(), request.name(), request.slug()));
        return ApiResponse.createdResponse("Category created", result);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse<CategoryResult>> update(
            @PathVariable String categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResult result = categoryService.update(
                new UpdateCategoryCommand(categoryId, request.name(), request.iconUrl(), request.active()));
        return ResponseEntity.ok(ApiResponse.success(result, "Category updated"));
    }

    @PostMapping("/{categoryId}/move")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Move category", description = "Reparent the category in the tree")
    public ResponseEntity<ApiResponse<CategoryResult>> move(
            @PathVariable String categoryId,
            @Valid @RequestBody MoveCategoryRequest request) {
        CategoryResult result = categoryService.move(new MoveCategoryCommand(categoryId, request.newParentId()));
        return ResponseEntity.ok(ApiResponse.success(result, "Category moved"));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Delete category", description = "Soft delete only")
    public ResponseEntity<Void> delete(@PathVariable String categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roots")
    @Operation(summary = "List root categories", description = "Public read - active root categories")
    public ResponseEntity<ApiResponse<List<CategoryResult>>> listRoots() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.listRoots(), "Root categories fetched"));
    }

    @GetMapping("/{categoryId}/children")
    @Operation(summary = "List children of a category", description = "Public read")
    public ResponseEntity<ApiResponse<List<CategoryResult>>> listChildren(@PathVariable String categoryId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.listChildren(categoryId), "Children fetched"));
    }

    @GetMapping("/tree")
    @Operation(summary = "Full category tree", description = "Public read - nested tree of all active categories")
    public ResponseEntity<ApiResponse<List<CategoryTreeNode>>> tree() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.tree(), "Category tree fetched"));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category", description = "Public read")
    public ResponseEntity<ApiResponse<CategoryResult>> get(@PathVariable String categoryId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.get(categoryId), "Category fetched"));
    }
}
