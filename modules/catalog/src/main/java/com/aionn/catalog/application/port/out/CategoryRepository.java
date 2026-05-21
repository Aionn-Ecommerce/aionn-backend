package com.aionn.catalog.application.port.out;

import com.aionn.catalog.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(String categoryId);

    boolean existsByParentAndName(String parentId, String name);

    boolean existsBySlug(String slug);

    /** Used to detect cycles when moving a category. */
    List<String> findDescendantIds(String categoryId);

    boolean hasProducts(String categoryId);

    List<Category> findChildren(String parentId);
}

