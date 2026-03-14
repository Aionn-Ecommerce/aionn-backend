package com.ecommerce.sharedkernel.domain.vo;

import com.ecommerce.sharedkernel.domain.model.ValueObject;

public record OffsetPagination(
        int page,
        int size,
        String sortBy,
        String sortDir) implements ValueObject {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public OffsetPagination {
        if (page < 0)
            throw new IllegalArgumentException("Page must be >= 0");
        if (size < 1)
            throw new IllegalArgumentException("Size must be >= 1");
        if (size > MAX_SIZE)
            throw new IllegalArgumentException("Size must be <= " + MAX_SIZE);

        sortDir = (sortDir != null && sortDir.equalsIgnoreCase("desc")) ? "desc" : "asc";
    }

    public static OffsetPagination of(int page, int size) {
        return new OffsetPagination(page, size, null, "asc");
    }

    public static OffsetPagination of(int page, int size, String sortBy, String sortDir) {
        return new OffsetPagination(page, size, sortBy, sortDir);
    }

    public static OffsetPagination defaultPage() {
        return of(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public int getOffset() {
        return page * size;
    }

    public boolean isFirstPage() {
        return page == 0;
    }

    public boolean isSortDesc() {
        return "desc".equals(sortDir);
    }

    public boolean hasSorting() {
        return sortBy != null && !sortBy.isBlank();
    }
}