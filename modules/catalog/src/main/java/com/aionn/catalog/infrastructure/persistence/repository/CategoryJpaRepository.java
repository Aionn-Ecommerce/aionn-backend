package com.aionn.catalog.infrastructure.persistence.repository;

import com.aionn.catalog.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, String> {

    boolean existsByParentIdAndNameIgnoreCase(String parentId, String name);

    boolean existsBySlug(String slug);

    List<CategoryEntity> findByParentId(String parentId);

    /**
     * Recursive descendant lookup. Postgres handles {@code WITH RECURSIVE}
     * natively which is exactly the sort of query we cannot reasonably
     * express through Spring Data method names.
     */
    @Query(value = """
            WITH RECURSIVE descendants AS (
                SELECT category_id FROM categories WHERE parent_id = :categoryId
                UNION ALL
                SELECT c.category_id FROM categories c
                  JOIN descendants d ON c.parent_id = d.category_id
            )
            SELECT category_id FROM descendants
            """, nativeQuery = true)
    List<String> findDescendantIds(String categoryId);
}

