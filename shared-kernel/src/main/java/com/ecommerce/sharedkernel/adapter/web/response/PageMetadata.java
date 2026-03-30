package com.ecommerce.sharedkernel.adapter.web.response;

public record PageMetadata(
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
