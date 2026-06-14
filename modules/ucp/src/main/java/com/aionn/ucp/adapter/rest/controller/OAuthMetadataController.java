package com.aionn.ucp.adapter.rest.controller;

import com.aionn.ucp.application.service.OAuthMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "UCP - Identity", description = "OAuth 2.0 metadata for UCP Identity Linking")
public class OAuthMetadataController {

    private final OAuthMetadataService metadataService;

    @GetMapping(value = "/.well-known/oauth-authorization-server", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get OAuth 2.0 Authorization Server Metadata (RFC 8414)", description = "Discovery endpoint for platforms to resolve authorization and token endpoints.")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        return ResponseEntity.ok(metadataService.buildMetadata());
    }
}
