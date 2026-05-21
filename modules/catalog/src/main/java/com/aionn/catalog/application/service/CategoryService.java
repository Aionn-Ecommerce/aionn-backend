package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.category.command.CreateCategoryCommand;
import com.aionn.catalog.application.dto.category.command.MoveCategoryCommand;
import com.aionn.catalog.application.dto.category.command.UpdateCategoryCommand;
import com.aionn.catalog.application.dto.category.result.CategoryResult;
import com.aionn.catalog.application.mapper.CategoryResultMapper;
import com.aionn.catalog.application.port.out.CategoryRepository;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.Category;
import com.aionn.sharedkernel.util.IdGenerator;
import com.aionn.sharedkernel.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryResultMapper categoryResultMapper;
    private final EventPublisher eventPublisher;

    public CategoryResult create(CreateCategoryCommand command) {
        if (command.parentId() != null && categoryRepository.findById(command.parentId()).isEmpty()) {
            throw new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND, "Parent category not found");
        }
        if (categoryRepository.existsByParentAndName(command.parentId(), command.name())) {
            throw new CatalogException(CatalogErrorCode.CATEGORY_NAME_CONFLICT);
        }
        String slug = command.slug() != null && !command.slug().isBlank()
                ? command.slug()
                : SlugUtils.slugify(command.name());
        if (categoryRepository.existsBySlug(slug)) {
            throw new CatalogException(CatalogErrorCode.CATEGORY_SLUG_CONFLICT);
        }
        Category category = Category.create(IdGenerator.ulid(), command.parentId(), command.name(), slug);
        Category saved = categoryRepository.save(category);
        eventPublisher.publish(category.pullEvents());
        return categoryResultMapper.toResult(saved);
    }

    public CategoryResult update(UpdateCategoryCommand command) {
        Category category = required(command.categoryId());
        if (command.name() != null
                && !command.name().equalsIgnoreCase(category.getName())
                && categoryRepository.existsByParentAndName(category.getParentId(), command.name())) {
            throw new CatalogException(CatalogErrorCode.CATEGORY_NAME_CONFLICT);
        }
        category.update(command.name(), command.iconUrl(), command.active());
        Category saved = categoryRepository.save(category);
        eventPublisher.publish(category.pullEvents());
        return categoryResultMapper.toResult(saved);
    }

    public CategoryResult move(MoveCategoryCommand command) {
        Category category = required(command.categoryId());
        if (command.newParentId() != null) {
            if (command.newParentId().equals(category.getCategoryId())) {
                throw new CatalogException(CatalogErrorCode.CATEGORY_CYCLE);
            }
            categoryRepository.findById(command.newParentId())
                    .orElseThrow(() -> new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND,
                            "Target parent does not exist"));
            // Cycle check: the new parent must not already be a descendant.
            if (categoryRepository.findDescendantIds(category.getCategoryId())
                    .contains(command.newParentId())) {
                throw new CatalogException(CatalogErrorCode.CATEGORY_CYCLE);
            }
        }
        category.moveTo(command.newParentId());
        Category saved = categoryRepository.save(category);
        eventPublisher.publish(category.pullEvents());
        return categoryResultMapper.toResult(saved);
    }

    public void delete(String categoryId) {
        Category category = required(categoryId);
        if (categoryRepository.hasProducts(categoryId)) {
            throw new CatalogException(CatalogErrorCode.CATEGORY_HAS_PRODUCTS);
        }
        category.markDeleted();
        categoryRepository.save(category);
        eventPublisher.publish(category.pullEvents());
    }

    public CategoryResult get(String categoryId) {
        return categoryResultMapper.toResult(required(categoryId));
    }

    private Category required(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND));
    }
}

