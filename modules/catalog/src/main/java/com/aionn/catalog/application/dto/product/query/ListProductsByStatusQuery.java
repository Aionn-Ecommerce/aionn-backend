package com.aionn.catalog.application.dto.product.query;

import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;

public record ListProductsByStatusQuery(ProductStatus status, OffsetPagination pagination) {
}
