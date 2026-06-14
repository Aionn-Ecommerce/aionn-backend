package com.aionn.catalog.infrastructure.persistence.mapper;

import com.aionn.catalog.domain.model.Brand;
import com.aionn.catalog.domain.valueobject.BrandStatus;
import com.aionn.catalog.infrastructure.persistence.entity.BrandEntity;
import org.springframework.stereotype.Component;

@Component
public class BrandDomainMapper {

    public BrandEntity toEntity(Brand brand) {
        BrandEntity entity = BrandEntity.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .logoUrl(brand.getLogoUrl())
                .description(brand.getDescription())
                .status(brand.getStatus().name())
                .build();

        java.util.List<com.aionn.catalog.infrastructure.persistence.entity.BrandTranslationEntity> translationEntities = new java.util.ArrayList<>();
        if (brand.translations() != null) {
            for (Brand.Translation trans : brand.translations()) {
                com.aionn.catalog.infrastructure.persistence.entity.BrandTranslationEntity te = com.aionn.catalog.infrastructure.persistence.entity.BrandTranslationEntity.builder()
                        .id(new com.aionn.catalog.infrastructure.persistence.entity.BrandTranslationEntity.BrandTranslationId(brand.getBrandId(), trans.locale()))
                        .brand(entity)
                        .name(trans.name())
                        .description(trans.description())
                        .build();
                translationEntities.add(te);
            }
        }
        entity.setTranslations(translationEntities);

        return entity;
    }

    public Brand toDomain(BrandEntity entity) {
        java.util.List<Brand.Translation> translations = new java.util.ArrayList<>();
        if (entity.getTranslations() != null) {
            for (com.aionn.catalog.infrastructure.persistence.entity.BrandTranslationEntity te : entity.getTranslations()) {
                translations.add(new Brand.Translation(te.getId().getLocale(), te.getName(), te.getDescription()));
            }
        }

        return new Brand(
                entity.getBrandId(),
                entity.getName(),
                entity.getLogoUrl(),
                entity.getDescription(),
                BrandStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                translations);
    }
}

