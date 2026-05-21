package com.aionn.catalog.domain.model;

import com.aionn.sharedkernel.domain.Guard;
import com.aionn.sharedkernel.domain.model.AggregateRoot;
import com.aionn.catalog.domain.event.AttributeTemplateEvents;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import lombok.Getter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class AttributeTemplate extends AggregateRoot {

    private final String templateId;
    private final String categoryId;
    private final Map<String, AttributeDefinition> attributes;
    private final Instant createdAt;
    private Instant updatedAt;

    public AttributeTemplate(
            String templateId,
            String categoryId,
            Map<String, AttributeDefinition> attributes,
            Instant createdAt,
            Instant updatedAt) {
        this.templateId = templateId;
        this.categoryId = categoryId;
        this.attributes = attributes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(attributes);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AttributeTemplate create(String templateId, String categoryId, List<String> attributeKeys) {
        Guard.require(attributeKeys != null && !attributeKeys.isEmpty(),
                () -> new CatalogException(CatalogErrorCode.INVALID_ARGUMENT, "attributes must not be empty"));
        Map<String, AttributeDefinition> initial = new LinkedHashMap<>();
        attributeKeys.forEach(key -> initial.put(key, new AttributeDefinition(key, true)));
        Instant now = Instant.now();
        AttributeTemplate template = new AttributeTemplate(templateId, categoryId, initial, now, now);
        template.record(new AttributeTemplateEvents.AttributeTemplateCreated(
                templateId, categoryId, List.copyOf(attributeKeys), now));
        return template;
    }

    public void configureFilterable(String attributeKey, boolean filterable) {
        Guard.require(attributes.containsKey(attributeKey),
                () -> new CatalogException(CatalogErrorCode.ATTRIBUTE_KEY_NOT_FOUND,
                        "Attribute not declared: " + attributeKey));
        attributes.put(attributeKey, new AttributeDefinition(attributeKey, filterable));
        this.updatedAt = Instant.now();
        record(new AttributeTemplateEvents.FilterableAttrConfigured(
                templateId, attributeKey, filterable, updatedAt));
    }

    public Map<String, AttributeDefinition> snapshot() {
        return Map.copyOf(attributes);
    }

    public record AttributeDefinition(String key, boolean filterable) {
    }

    @Override
    protected String aggregateId() {
        return templateId;
    }
}
