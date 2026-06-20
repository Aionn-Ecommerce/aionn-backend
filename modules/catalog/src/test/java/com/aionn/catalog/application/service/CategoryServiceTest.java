package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.category.command.CreateCategoryCommand;
import com.aionn.catalog.application.dto.category.command.MoveCategoryCommand;
import com.aionn.catalog.application.dto.category.result.CategoryResult;
import com.aionn.catalog.application.dto.category.result.CategoryTreeNode;
import com.aionn.catalog.application.mapper.CategoryResultMapper;
import com.aionn.catalog.application.port.out.CategoryPersistencePort;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.Category;
import com.aionn.sharedkernel.application.port.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryPersistencePort categoryRepository;
    @Mock
    private CategoryResultMapper categoryResultMapper;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryResult sampleResult;

    @BeforeEach
    void setUp() {
        sampleResult = new CategoryResult(
                "01HZCAT0000000000000000001", null, "Electronics", "electronics",
                null, true, Instant.now(), Instant.now());
    }

    @Test
    void createPersistsCategoryWhenNameAndSlugAreFree() {
        when(categoryRepository.existsByParentAndName(null, "Electronics")).thenReturn(false);
        when(categoryRepository.existsBySlug("electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoryResultMapper.toResult(any(Category.class))).thenReturn(sampleResult);

        CategoryResult result = categoryService.create(
                new CreateCategoryCommand(null, "Electronics", "electronics"));

        assertThat(result).isEqualTo(sampleResult);
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Electronics");
        verify(eventPublisher).publish(anyCollection());
    }

    @Test
    void createThrowsOnNameConflict() {
        when(categoryRepository.existsByParentAndName(null, "Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(
                new CreateCategoryCommand(null, "Electronics", "electronics")))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.CATEGORY_NAME_CONFLICT.getCode());

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createThrowsOnSlugConflict() {
        when(categoryRepository.existsByParentAndName(null, "Electronics")).thenReturn(false);
        when(categoryRepository.existsBySlug("electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(
                new CreateCategoryCommand(null, "Electronics", "electronics")))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.CATEGORY_SLUG_CONFLICT.getCode());
    }

    @Test
    void moveRejectsCycleWhenTargetParentIsDescendant() {
        Category category = Category.create("cat-1", null, "A", "a");
        category.pullEvents();
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(categoryRepository.findById("cat-2"))
                .thenReturn(Optional.of(Category.create("cat-2", "cat-1", "B", "b")));
        when(categoryRepository.findDescendantIds("cat-1")).thenReturn(List.of("cat-2"));

        assertThatThrownBy(() -> categoryService.move(new MoveCategoryCommand("cat-1", "cat-2")))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.CATEGORY_CYCLE.getCode());
    }

    @Test
    void deleteRejectsCategoryWithProducts() {
        Category category = Category.create("cat-1", null, "A", "a");
        category.pullEvents();
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(categoryRepository.hasProducts("cat-1")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete("cat-1"))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.CATEGORY_HAS_PRODUCTS.getCode());
    }

    @Test
    void treeBuildsHierarchyFromActiveCategories() {
        Category root = Category.create("root", null, "Root", "root");
        Category child = Category.create("child", "root", "Child", "child");
        when(categoryRepository.findAllActive()).thenReturn(List.of(root, child));

        CategoryResult rootResult = new CategoryResult(
                "root", null, "Root", "root", null, true, Instant.now(), Instant.now());
        CategoryResult childResult = new CategoryResult(
                "child", "root", "Child", "child", null, true, Instant.now(), Instant.now());
        when(categoryResultMapper.toResult(root)).thenReturn(rootResult);
        when(categoryResultMapper.toResult(child)).thenReturn(childResult);

        List<CategoryTreeNode> tree = categoryService.tree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).category().categoryId()).isEqualTo("root");
        assertThat(tree.get(0).children()).hasSize(1);
        assertThat(tree.get(0).children().get(0).category().categoryId()).isEqualTo("child");
    }
}
