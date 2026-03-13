package com.ecommerce.sharedkernel.domain.model;

import java.util.Objects;

public abstract class BaseEntity<ID> {
    private final ID id;

    protected BaseEntity(ID id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public ID getId() {
        return id;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseEntity<?> that = (BaseEntity<?>) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
