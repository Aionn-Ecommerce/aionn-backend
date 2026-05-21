package com.aionn.identity.adapter.rest.controller;

import com.aionn.identity.adapter.rest.dto.geography.GeographyResponse;
import com.aionn.identity.adapter.rest.mapper.geography.GeographyDtoMapper;
import com.aionn.identity.application.service.GeographyService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
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
        return ResponseEntity.ok(ApiResponse.success(geographyDtoMapper.toResponses(result), "Countries fetched"));
    }

    @GetMapping("/countries/{code}")
    @Operation(summary = "Get country", description = "Get country by code")
    public ResponseEntity<ApiResponse<GeographyResponse>> getCountry(@PathVariable String code) {
        var result = geographyService.getCountry(code);
        return ResponseEntity.ok(ApiResponse.success(geographyDtoMapper.toResponse(result), "Country fetched"));
    }

    @GetMapping("/provinces")
    @Operation(summary = "List provinces", description = "Get all active provinces, optionally filtered by country")
    public ResponseEntity<ApiResponse<List<GeographyResponse>>> listProvinces(
            @RequestParam(required = false) String countryCode) {
        var result = geographyService.listProvinces(countryCode);
        return ResponseEntity.ok(ApiResponse.success(geographyDtoMapper.toResponses(result), "Provinces fetched"));
    }

    @GetMapping("/provinces/{code}")
    @Operation(summary = "Get province", description = "Get province by code")
    public ResponseEntity<ApiResponse<GeographyResponse>> getProvince(@PathVariable String code) {
        var result = geographyService.getProvince(code);
        return ResponseEntity.ok(ApiResponse.success(geographyDtoMapper.toResponse(result), "Province fetched"));
    }

    @GetMapping("/districts")
    @Operation(summary = "List districts", description = "Get all active districts by province code")
    public ResponseEntity<ApiResponse<List<GeographyResponse>>> listDistricts(
            @RequestParam String provinceCode) {
        var result = geographyService.listDistricts(provinceCode);
        return ResponseEntity.ok(ApiResponse.success(geographyDtoMapper.toResponses(result), "Districts fetched"));
    }

    @GetMapping("/districts/{code}")
    @Operation(summary = "Get district", description = "Get district by code")
    public ResponseEntity<ApiResponse<GeographyResponse>> getDistrict(@PathVariable String code) {
        var result = geographyService.getDistrict(code);
        return ResponseEntity.ok(ApiResponse.success(geographyDtoMapper.toResponse(result), "District fetched"));
    }

    @GetMapping("/wards")
    @Operation(summary = "List wards", description = "Get all active wards by district code")
    public ResponseEntity<ApiResponse<List<GeographyResponse>>> listWards(
            @RequestParam String districtCode) {
        var result = geographyService.listWards(districtCode);
        return ResponseEntity.ok(ApiResponse.success(geographyDtoMapper.toResponses(result), "Wards fetched"));
    }

    @GetMapping("/wards/{code}")
    @Operation(summary = "Get ward", description = "Get ward by code")
    public ResponseEntity<ApiResponse<GeographyResponse>> getWard(@PathVariable String code) {
        var result = geographyService.getWard(code);
        return ResponseEntity.ok(ApiResponse.success(geographyDtoMapper.toResponse(result), "Ward fetched"));
    }
}

