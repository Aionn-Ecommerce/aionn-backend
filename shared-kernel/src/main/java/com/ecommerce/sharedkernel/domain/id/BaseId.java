package com.ecommerce.sharedkernel.domain.id;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseId implements Serializable {

    private final UUID value;

    protected BaseId(UUID value) {
        this.value = Objects.requireNonNull(value, "ID value must not be null");
    }

    protected BaseId(String value) {
        Objects.requireNonNull(value, "ID value must not be null");
        this.value = UUID.fromString(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BaseId baseId = (BaseId) o;
        return Objects.equals(value, baseId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
