package com.aionn.promotion.adapter.rest.controller;

import com.aionn.promotion.application.dto.banner.result.PromotionBannerResult;
import com.aionn.promotion.application.service.PromotionBannerService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions/banners")
@RequiredArgsConstructor
@Tag(name = "Promotion - Banner", description = "Public promotion banners")
public class PromotionBannerController {

    private final PromotionBannerService bannerService;

    @GetMapping
    @Operation(summary = "Get active promotion banners")
    public ResponseEntity<ApiResponse<List<PromotionBannerResult>>> getActiveBanners() {
        return ResponseEntity.ok(ApiResponse.success(
                bannerService.listActive(), "Promotion banners fetched"));
    }
}
