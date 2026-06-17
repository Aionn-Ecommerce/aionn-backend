package com.aionn.catalog.infrastructure.persistence.adapter.category;

import com.aionn.catalog.application.port.out.CategoryPersistencePort;
import com.aionn.catalog.domain.model.Category;
import com.aionn.catalog.infrastructure.persistence.mapper.CategoryDomainMapper;
import com.aionn.catalog.infrastructure.persistence.repository.CategoryRepository;
import com.aionn.catalog.infrastructure.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryPersistencePort {

    private final CategoryRepository jpa;
    private final ProductRepository productJpa;
    private final CategoryDomainMapper mapper;

    @Override
    public Category save(Category category) {
        var saved = jpa.save(mapper.toEntity(category));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(String categoryId) {
        return jpa.findById(categoryId).map(mapper::toDomain);
    }

    @Override
    public List<Category> findAllByIds(java.util.Collection<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        return jpa.findAllById(categoryIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByParentAndName(String parentId, String name) {
        return jpa.existsByParentIdAndNameIgnoreCase(parentId, name);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpa.existsBySlug(slug);
    }

    @Override
    public List<String> findDescendantIds(String categoryId) {
        return jpa.findDescendantIds(categoryId);
    }

    @Override
    public boolean hasProducts(String categoryId) {
        return productJpa.existsByCategoryId(categoryId);
    }

    @Override
    public List<Category> findChildren(String parentId) {
        return jpa.findByParentId(parentId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findActiveRoots() {
        return jpa.findActiveRoots().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findActiveChildren(String parentId) {
        return jpa.findActiveByParentId(parentId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findAllActive() {
        return jpa.findAllActive().stream()
                .map(mapper::toDomain)
                .toList();
    }
}

