package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.attribute.command.ConfigureFilterableCommand;
import com.aionn.catalog.application.dto.attribute.command.CreateAttributeTemplateCommand;
import com.aionn.catalog.application.dto.attribute.result.AttributeTemplateResult;
import com.aionn.catalog.application.port.out.AttributeTemplatePersistencePort;
import com.aionn.catalog.application.port.out.CategoryPersistencePort;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.AttributeTemplate;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AttributeTemplateService {

    private final AttributeTemplatePersistencePort attributeTemplateRepository;
    private final CategoryPersistencePort categoryRepository;
    private final EventPublisher eventPublisher;

    public AttributeTemplateResult create(CreateAttributeTemplateCommand command) {
        categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND));
        AttributeTemplate template = AttributeTemplate.create(IdGenerator.ulid(),
                command.categoryId(), command.attributeKeys());
        AttributeTemplate saved = attributeTemplateRepository.save(template);
        eventPublisher.publish(template.pullEvents());
        return toResult(saved);
    }

    public AttributeTemplateResult configureFilterable(ConfigureFilterableCommand command) {
        AttributeTemplate template = attributeTemplateRepository.findById(command.templateId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.ATTRIBUTE_TEMPLATE_NOT_FOUND));
        template.configureFilterable(command.attributeKey(), command.filterable());
        AttributeTemplate saved = attributeTemplateRepository.save(template);
        eventPublisher.publish(template.pullEvents());
        return toResult(saved);
    }

    @Transactional(readOnly = true)
    public AttributeTemplateResult get(String templateId) {
        return toResult(attributeTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.ATTRIBUTE_TEMPLATE_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public AttributeTemplateResult getByCategory(String categoryId) {
        return toResult(attributeTemplateRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.ATTRIBUTE_TEMPLATE_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public java.util.Optional<AttributeTemplateResult> findByCategoryId(String categoryId) {
        return attributeTemplateRepository.findByCategoryId(categoryId).map(this::toResult);
    }

    private AttributeTemplateResult toResult(AttributeTemplate template) {
        Map<String, Boolean> attrs = new LinkedHashMap<>();
        template.snapshot().forEach((k, v) -> attrs.put(k, v.filterable()));
        return new AttributeTemplateResult(
                template.getTemplateId(),
                template.getCategoryId(),
                attrs,
                template.getCreatedAt(),
                template.getUpdatedAt());
    }
}
