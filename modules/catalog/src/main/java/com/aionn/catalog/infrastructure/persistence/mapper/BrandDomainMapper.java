package com.aionn.catalog.infrastructure.persistence.mapper;

import com.aionn.catalog.domain.model.Brand;
import com.aionn.catalog.domain.valueobject.BrandStatus;
import com.aionn.catalog.infrastructure.persistence.entity.BrandEntity;
import org.springframework.stereotype.Component;

@Component
public class BrandDomainMapper {

    public BrandEntity toEntity(Brand brand) {
        return BrandEntity.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .logoUrl(brand.getLogoUrl())
                .description(brand.getDescription())
                .status(brand.getStatus().name())
                .build();
    }

    public Brand toDomain(BrandEntity entity) {
        return new Brand(
                entity.getBrandId(),
                entity.getName(),
                entity.getLogoUrl(),
                entity.getDescription(),
                BrandStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}

