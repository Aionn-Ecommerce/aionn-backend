package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.attribute.ConfigureFilterableRequest;
import com.aionn.catalog.adapter.rest.dto.attribute.CreateAttributeTemplateRequest;
import com.aionn.catalog.application.dto.attribute.command.ConfigureFilterableCommand;
import com.aionn.catalog.application.dto.attribute.command.CreateAttributeTemplateCommand;
import com.aionn.catalog.application.dto.attribute.result.AttributeTemplateResult;
import com.aionn.catalog.application.service.AttributeTemplateService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog/attribute-templates")
@RequiredArgsConstructor
@Tag(name = "Catalog - AttributeTemplate", description = "Catalog module: category attribute templates")
public class AttributeTemplateController {

    private final AttributeTemplateService attributeTemplateService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Create attribute template", description = "UC3.30")
    public ResponseEntity<ApiResponse<AttributeTemplateResult>> create(
            @Valid @RequestBody CreateAttributeTemplateRequest request) {
        AttributeTemplateResult result = attributeTemplateService.create(
                new CreateAttributeTemplateCommand(request.categoryId(), request.attributeKeys()));
        return ApiResponse.createdResponse("Attribute template created", result);
    }

    @PutMapping("/{templateId}/filterable")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Configure filterable", description = "UC3.31 / UC3.33 - mark a key as filterable for the AI agent")
    public ResponseEntity<ApiResponse<AttributeTemplateResult>> configureFilterable(
            @PathVariable String templateId,
            @Valid @RequestBody ConfigureFilterableRequest request) {
        AttributeTemplateResult result = attributeTemplateService.configureFilterable(
                new ConfigureFilterableCommand(templateId, request.attributeKey(), request.filterable()));
        return ResponseEntity.ok(ApiResponse.success(result, "Filterable updated"));
    }

    @GetMapping("/{templateId}")
    @Operation(summary = "Get attribute template", description = "Public read")
    public ResponseEntity<ApiResponse<AttributeTemplateResult>> get(@PathVariable String templateId) {
        return ResponseEntity.ok(ApiResponse.success(
                attributeTemplateService.get(templateId), "Attribute template fetched"));
    }
}

