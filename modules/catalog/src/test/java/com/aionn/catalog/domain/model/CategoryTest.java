package com.aionn.catalog.domain.model;

import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    private static final String CATEGORY_ID = "01HZCAT0000000000000000001";

    @Test
    void createSetsActiveTrueAndEmitsCreatedEvent() {
        Category category = Category.create(CATEGORY_ID, null, "Electronics", "electronics");

        assertThat(category.isActive()).isTrue();
        assertThat(category.getDeletedAt()).isNull();
        assertThat(category.pullEvents()).hasSize(1);
    }

    @Test
    void createRejectsBlankName() {
        assertThatThrownBy(() -> Category.create(CATEGORY_ID, null, "", "x"))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.INVALID_ARGUMENT.getCode());
    }

    @Test
    void moveToSelfThrowsCycle() {
        Category category = Category.create(CATEGORY_ID, null, "Electronics", "electronics");

        assertThatThrownBy(() -> category.moveTo(CATEGORY_ID))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.CATEGORY_CYCLE.getCode());
    }

    @Test
    void markDeletedSetsTimestampAndDeactivates() {
        Category category = Category.create(CATEGORY_ID, null, "Electronics", "electronics");

        category.markDeleted();

        assertThat(category.getDeletedAt()).isNotNull();
        assertThat(category.isActive()).isFalse();
    }

    @Test
    void updateOnDeletedCategoryThrows() {
        Category category = Category.create(CATEGORY_ID, null, "Electronics", "electronics");
        category.markDeleted();

        assertThatThrownBy(() -> category.update("Renamed", null, true))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.CATEGORY_NOT_FOUND.getCode());
    }
}
