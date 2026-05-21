package com.aionn.catalog.application.mapper;

import com.aionn.catalog.application.dto.brand.result.BrandResult;
import com.aionn.catalog.domain.model.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandResultMapper {

    public BrandResult toResult(Brand brand) {
        return new BrandResult(
                brand.getBrandId(),
                brand.getName(),
                brand.getLogoUrl(),
                brand.getDescription(),
                brand.getStatus().name(),
                brand.getCreatedAt(),
                brand.getUpdatedAt());
    }
}

