package com.aionn.catalog.infrastructure.persistence.mapper;

import com.aionn.catalog.domain.model.Category;
import com.aionn.catalog.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryDomainMapper {

    public CategoryEntity toEntity(Category category) {
        CategoryEntity entity = CategoryEntity.builder()
                .categoryId(category.getCategoryId())
                .parentId(category.getParentId())
                .name(category.getName())
                .slug(category.getSlug())
                .iconUrl(category.getIconUrl())
                .active(category.isActive())
                .deletedAt(category.getDeletedAt())
                .build();

        java.util.List<com.aionn.catalog.infrastructure.persistence.entity.CategoryTranslationEntity> translationEntities = new java.util.ArrayList<>();
        if (category.translations() != null) {
            for (Category.Translation trans : category.translations()) {
                com.aionn.catalog.infrastructure.persistence.entity.CategoryTranslationEntity te = com.aionn.catalog.infrastructure.persistence.entity.CategoryTranslationEntity.builder()
                        .id(new com.aionn.catalog.infrastructure.persistence.entity.CategoryTranslationEntity.CategoryTranslationId(category.getCategoryId(), trans.locale()))
                        .category(entity)
                        .name(trans.name())
                        .build();
                translationEntities.add(te);
            }
        }
        entity.setTranslations(translationEntities);

        return entity;
    }

    public Category toDomain(CategoryEntity entity) {
        java.util.List<Category.Translation> translations = new java.util.ArrayList<>();
        if (entity.getTranslations() != null) {
            for (com.aionn.catalog.infrastructure.persistence.entity.CategoryTranslationEntity te : entity.getTranslations()) {
                translations.add(new Category.Translation(te.getId().getLocale(), te.getName()));
            }
        }

        return new Category(
                entity.getCategoryId(),
                entity.getParentId(),
                entity.getName(),
                entity.getSlug(),
                entity.getIconUrl(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                translations);
    }
}

