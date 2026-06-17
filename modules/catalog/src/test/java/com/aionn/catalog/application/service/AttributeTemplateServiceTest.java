package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.attribute.command.ConfigureFilterableCommand;
import com.aionn.catalog.application.dto.attribute.command.CreateAttributeTemplateCommand;
import com.aionn.catalog.application.dto.attribute.result.AttributeTemplateResult;
import com.aionn.catalog.application.port.out.AttributeTemplatePersistencePort;
import com.aionn.catalog.application.port.out.CategoryPersistencePort;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.AttributeTemplate;
import com.aionn.catalog.domain.model.Category;
import com.aionn.sharedkernel.application.port.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class AttributeTemplateServiceTest {

    @Mock
    private AttributeTemplatePersistencePort attributeTemplateRepository;
    @Mock
    private CategoryPersistencePort categoryRepository;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private AttributeTemplateService attributeTemplateService;

    @Test
    void createPersistsTemplateWithFilterableAttributesAndPublishes() {
        Category category = Category.create("01HZCAT0000000000000000001", null, "Electronics", "electronics");
        when(categoryRepository.findById("01HZCAT0000000000000000001")).thenReturn(Optional.of(category));
        when(attributeTemplateRepository.save(any(AttributeTemplate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AttributeTemplateResult result = attributeTemplateService.create(
                new CreateAttributeTemplateCommand("01HZCAT0000000000000000001", List.of("color", "size")));

        assertThat(result).isNotNull();
        assertThat(result.categoryId()).isEqualTo("01HZCAT0000000000000000001");
        assertThat(result.attributes()).containsKeys("color", "size");
        assertThat(result.attributes().get("color")).isTrue();
        verify(attributeTemplateRepository).save(any(AttributeTemplate.class));
        verify(eventPublisher).publish(anyCollection());
    }

    @Test
    void createThrowsWhenCategoryMissing() {
        when(categoryRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attributeTemplateService.create(
                new CreateAttributeTemplateCommand("missing", List.of("color"))))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.CATEGORY_NOT_FOUND.getCode());

        verify(attributeTemplateRepository, never()).save(any());
    }

    @Test
    void configureFilterableUpdatesExistingAttribute() {
        AttributeTemplate template = AttributeTemplate.create(
                "01HZTPL0000000000000000001", "01HZCAT0000000000000000001", List.of("color"));
        template.pullEvents();
        when(attributeTemplateRepository.findById("01HZTPL0000000000000000001"))
                .thenReturn(Optional.of(template));
        when(attributeTemplateRepository.save(template)).thenReturn(template);

        AttributeTemplateResult result = attributeTemplateService.configureFilterable(
                new ConfigureFilterableCommand("01HZTPL0000000000000000001", "color", false));

        assertThat(result.attributes().get("color")).isFalse();
        verify(eventPublisher).publish(anyCollection());
    }

    @Test
    void getThrowsWhenTemplateMissing() {
        when(attributeTemplateRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attributeTemplateService.get("nope"))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.ATTRIBUTE_TEMPLATE_NOT_FOUND.getCode());
    }
}
