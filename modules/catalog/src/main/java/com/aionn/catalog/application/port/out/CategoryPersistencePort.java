package com.aionn.catalog.application.port.out;

import com.aionn.catalog.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryPersistencePort {

    Category save(Category category);

    Optional<Category> findById(String categoryId);

    List<Category> findAllByIds(java.util.Collection<String> categoryIds);

    boolean existsByParentAndName(String parentId, String name);

    boolean existsBySlug(String slug);

    List<String> findDescendantIds(String categoryId);

    boolean hasProducts(String categoryId);

    List<Category> findChildren(String parentId);

    List<Category> findActiveRoots();

    List<Category> findActiveChildren(String parentId);

    List<Category> findAllActive();
}
