package com.aionn.ucp.adapter.rest.controller;

import com.aionn.ucp.application.dto.profile.BusinessProfileDto;
import com.aionn.ucp.application.service.BusinessProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "UCP - Discovery", description = "UCP business profile discovery (/.well-known/ucp)")
public class WellKnownUcpController {

    private final BusinessProfileService businessProfileService;

    @GetMapping(value = "/.well-known/ucp", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get business UCP profile", description = "Public discovery endpoint advertising services, capabilities and signing keys.")
    public ResponseEntity<BusinessProfileDto> getProfile() {
        return ResponseEntity.ok(businessProfileService.buildProfile());
    }
}
