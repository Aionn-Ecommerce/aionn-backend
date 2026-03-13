package com.ecommerce.sharedkernel.domain.model;

public record EntityId(String value) {
    public EntityId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Entity id must not be null or blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
