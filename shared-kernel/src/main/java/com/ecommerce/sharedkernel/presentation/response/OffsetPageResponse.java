package com.ecommerce.sharedkernel.presentation.response;

import com.ecommerce.sharedkernel.domain.vo.OffsetPagination;
import java.util.List;

public record OffsetPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> OffsetPageResponse<T> of(List<T> content, OffsetPagination pagination, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pagination.size());
        return new OffsetPageResponse<>(
                content,
                pagination.page(),
                pagination.size(),
                totalElements,
                totalPages,
                pagination.isFirstPage(),
                pagination.page() >= totalPages - 1);
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}