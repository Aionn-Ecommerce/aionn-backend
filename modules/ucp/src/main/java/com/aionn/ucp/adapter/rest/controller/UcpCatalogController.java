package com.aionn.ucp.adapter.rest.controller;

import com.aionn.ucp.application.dto.catalog.CatalogRequests;
import com.aionn.ucp.application.dto.catalog.CatalogResponses;
import com.aionn.ucp.application.service.UcpCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/ucp/v1/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "UCP - Catalog", description = "UCP catalog capability (search, lookup, product detail)")
public class UcpCatalogController {

    private final UcpCatalogService catalogService;

    @PostMapping("/search")
    @Operation(summary = "Search catalog (UCP)")
    public ResponseEntity<CatalogResponses.SearchResponse> search(
            @Valid @RequestBody CatalogRequests.SearchRequest request) {
        return ResponseEntity.ok(catalogService.search(request));
    }

    @PostMapping("/lookup")
    @Operation(summary = "Batch lookup products (UCP)")
    public ResponseEntity<CatalogResponses.LookupResponse> lookup(
            @Valid @RequestBody CatalogRequests.LookupRequest request) {
        return ResponseEntity.ok(catalogService.lookup(request));
    }

    @PostMapping("/product")
    @Operation(summary = "Get product detail (UCP)")
    public ResponseEntity<CatalogResponses.GetProductResponse> getProduct(
            @Valid @RequestBody CatalogRequests.GetProductRequest request) {
        return ResponseEntity.ok(catalogService.getProduct(request));
    }
}
