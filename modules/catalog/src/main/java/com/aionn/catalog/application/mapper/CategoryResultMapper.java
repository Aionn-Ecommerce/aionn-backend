package com.aionn.catalog.application.mapper;

import com.aionn.catalog.application.dto.category.result.CategoryResult;
import com.aionn.catalog.domain.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryResultMapper {

    public CategoryResult toResult(Category category) {
        return new CategoryResult(
                category.getCategoryId(),
                category.getParentId(),
                category.getName(),
                category.getSlug(),
                category.getIconUrl(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}

