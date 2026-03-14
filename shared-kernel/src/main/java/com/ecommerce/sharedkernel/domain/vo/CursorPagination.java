package com.ecommerce.sharedkernel.domain.vo;

import com.ecommerce.sharedkernel.domain.model.ValueObject;
import java.util.Base64;

public record CursorPagination(
        String cursor,
        int size,
        String sortDir) implements ValueObject {

    public static final int DEFAULT_SIZE = 20;

    public CursorPagination {
        if (size < 1)
            throw new IllegalArgumentException("Size must be >= 1");
        sortDir = (sortDir != null && sortDir.equalsIgnoreCase("asc")) ? "asc" : "desc";
    }

    public static CursorPagination of(String cursor, int size) {
        return new CursorPagination(cursor, size, "desc");
    }

    public boolean isFirstPage() {
        return cursor == null || cursor.isBlank();
    }

    public String getDecodedCursor() {
        if (isFirstPage())
            return null;
        try {
            return new String(Base64.getDecoder().decode(cursor));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor format");
        }
    }
}