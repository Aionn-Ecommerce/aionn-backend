package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.adapter.rest.dto.geography.GeographyResponse;
import com.ecommerce.identity.adapter.rest.mapper.geography.GeographyDtoMapper;
import com.ecommerce.identity.application.service.GeographyService;
import com.ecommerce.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/geography")
@RequiredArgsConstructor
@Tag(name = "Geography", description = "Geographic reference data endpoints - countries, provinces, districts, wards")
public class GeographyController {

    private final GeographyService geographyService;
    private final GeographyDtoMapper geographyDtoMapper;

    @GetMapping("/countries")
    @Operation(summary = "List countries", description = "Get all active countries")
    public ResponseEntity<ApiResponse<List<GeographyResponse>>> listCountries() {
        var result = geographyService.listCountries();
        var response = geographyDtoMapper.toResponses(result);
        return ResponseEntity.ok(ApiResponse.success(response, "Countries fetched"));
    }

    @GetMapping("/countries/{code}")
    @Operation(summary = "Get country", description = "Get country by code")
    public ResponseEntity<ApiResponse<GeographyResponse>> getCountry(@PathVariable String code) {
        var result = geographyService.getCountry(code)
                .orElseThrow(() -> new IllegalArgumentException("Country not found: " + code));
        var response = geographyDtoMapper.toResponse(result);
        return ResponseEntity.ok(ApiResponse.success(response, "Country fetched"));
    }

    @GetMapping("/provinces")
    @Operation(summary = "List provinces", description = "Get all active provinces, optionally filtered by country")
    public ResponseEntity<ApiResponse<List<GeographyResponse>>> listProvinces(
            @RequestParam(required = false) String countryCode) {
        var result = geographyService.listProvinces(countryCode);
        var response = geographyDtoMapper.toResponses(result);
        return ResponseEntity.ok(ApiResponse.success(response, "Provinces fetched"));
    }

    @GetMapping("/provinces/{code}")
    @Operation(summary = "Get province", description = "Get province by code")
    public ResponseEntity<ApiResponse<GeographyResponse>> getProvince(@PathVariable String code) {
        var result = geographyService.getProvince(code)
                .orElseThrow(() -> new IllegalArgumentException("Province not found: " + code));
        var response = geographyDtoMapper.toResponse(result);
        return ResponseEntity.ok(ApiResponse.success(response, "Province fetched"));
    }

    @GetMapping("/districts")
    @Operation(summary = "List districts", description = "Get all active districts by province code")
    public ResponseEntity<ApiResponse<List<GeographyResponse>>> listDistricts(
            @RequestParam String provinceCode) {
        var result = geographyService.listDistricts(provinceCode);
        var response = geographyDtoMapper.toResponses(result);
        return ResponseEntity.ok(ApiResponse.success(response, "Districts fetched"));
    }

    @GetMapping("/districts/{code}")
    @Operation(summary = "Get district", description = "Get district by code")
    public ResponseEntity<ApiResponse<GeographyResponse>> getDistrict(@PathVariable String code) {
        var result = geographyService.getDistrict(code)
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + code));
        var response = geographyDtoMapper.toResponse(result);
        return ResponseEntity.ok(ApiResponse.success(response, "District fetched"));
    }

    @GetMapping("/wards")
    @Operation(summary = "List wards", description = "Get all active wards by district code")
    public ResponseEntity<ApiResponse<List<GeographyResponse>>> listWards(
            @RequestParam String districtCode) {
        var result = geographyService.listWards(districtCode);
        var response = geographyDtoMapper.toResponses(result);
        return ResponseEntity.ok(ApiResponse.success(response, "Wards fetched"));
    }

    @GetMapping("/wards/{code}")
    @Operation(summary = "Get ward", description = "Get ward by code")
    public ResponseEntity<ApiResponse<GeographyResponse>> getWard(@PathVariable String code) {
        var result = geographyService.getWard(code)
                .orElseThrow(() -> new IllegalArgumentException("Ward not found: " + code));
        var response = geographyDtoMapper.toResponse(result);
        return ResponseEntity.ok(ApiResponse.success(response, "Ward fetched"));
    }
}


