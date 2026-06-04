package com.aionn.catalog.infrastructure.persistence.adapter.attribute;

import com.aionn.catalog.application.port.out.AttributeTemplateRepository;
import com.aionn.catalog.domain.model.AttributeTemplate;
import com.aionn.catalog.infrastructure.persistence.mapper.AttributeTemplateDomainMapper;
import com.aionn.catalog.infrastructure.persistence.repository.AttributeTemplateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttributeTemplateRepositoryAdapter implements AttributeTemplateRepository {

    private final AttributeTemplateJpaRepository jpa;
    private final AttributeTemplateDomainMapper mapper;

    @Override
    public AttributeTemplate save(AttributeTemplate template) {
        return mapper.toDomain(jpa.save(mapper.toEntity(template)));
    }

    @Override
    public Optional<AttributeTemplate> findById(String templateId) {
        return jpa.findById(templateId).map(mapper::toDomain);
    }

    @Override
    public Optional<AttributeTemplate> findByCategoryId(String categoryId) {
        return jpa.findByCategoryId(categoryId).map(mapper::toDomain);
    }
}

