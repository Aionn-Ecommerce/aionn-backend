package com.aionn.catalog.domain.model;

import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttributeTemplateTest {

    private static final String TEMPLATE_ID = "01HZTPL0000000000000000001";
    private static final String CATEGORY_ID = "01HZCAT0000000000000000001";

    @Test
    void createInitializesAttributesAsFilterableAndEmitsEvent() {
        AttributeTemplate template = AttributeTemplate.create(
                TEMPLATE_ID, CATEGORY_ID, List.of("color", "size"));

        assertThat(template.getTemplateId()).isEqualTo(TEMPLATE_ID);
        assertThat(template.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(template.snapshot()).hasSize(2);
        assertThat(template.snapshot().get("color").filterable()).isTrue();
        assertThat(template.snapshot().get("size").filterable()).isTrue();
        assertThat(template.pullEvents()).hasSize(1);
    }

    @Test
    void createRejectsEmptyAttributes() {
        assertThatThrownBy(() -> AttributeTemplate.create(TEMPLATE_ID, CATEGORY_ID, List.of()))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.INVALID_ARGUMENT.getCode());
    }

    @Test
    void configureFilterableUpdatesAttributeFlag() {
        AttributeTemplate template = AttributeTemplate.create(
                TEMPLATE_ID, CATEGORY_ID, List.of("color"));
        template.pullEvents();

        template.configureFilterable("color", false);

        assertThat(template.snapshot().get("color").filterable()).isFalse();
        assertThat(template.pullEvents()).hasSize(1);
    }

    @Test
    void configureFilterableThrowsWhenAttributeMissing() {
        AttributeTemplate template = AttributeTemplate.create(
                TEMPLATE_ID, CATEGORY_ID, List.of("color"));

        assertThatThrownBy(() -> template.configureFilterable("missing", true))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.ATTRIBUTE_KEY_NOT_FOUND.getCode());
    }
}
