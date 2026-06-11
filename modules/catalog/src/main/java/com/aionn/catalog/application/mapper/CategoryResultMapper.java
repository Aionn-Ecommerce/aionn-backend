package com.aionn.catalog.application.mapper;

import com.aionn.catalog.application.dto.category.result.CategoryResult;
import com.aionn.catalog.domain.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryResultMapper {

    CategoryResult toResult(Category category);
}
