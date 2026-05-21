package com.aionn.catalog.infrastructure.persistence.mapper;

import com.aionn.catalog.domain.model.Category;
import com.aionn.catalog.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryDomainMapper {

    public CategoryEntity toEntity(Category category) {
        return CategoryEntity.builder()
                .categoryId(category.getCategoryId())
                .parentId(category.getParentId())
                .name(category.getName())
                .slug(category.getSlug())
                .iconUrl(category.getIconUrl())
                .active(category.isActive())
                .deletedAt(category.getDeletedAt())
                .build();
    }

    public Category toDomain(CategoryEntity entity) {
        return new Category(
                entity.getCategoryId(),
                entity.getParentId(),
                entity.getName(),
                entity.getSlug(),
                entity.getIconUrl(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}

