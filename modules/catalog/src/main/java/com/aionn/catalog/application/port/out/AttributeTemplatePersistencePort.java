package com.aionn.catalog.application.port.out;

import com.aionn.catalog.domain.model.AttributeTemplate;

import java.util.Optional;

public interface AttributeTemplatePersistencePort {

    AttributeTemplate save(AttributeTemplate template);

    Optional<AttributeTemplate> findById(String templateId);

    Optional<AttributeTemplate> findByCategoryId(String categoryId);

    java.util.List<AttributeTemplate> findByCategoryIds(java.util.Collection<String> categoryIds);
}

