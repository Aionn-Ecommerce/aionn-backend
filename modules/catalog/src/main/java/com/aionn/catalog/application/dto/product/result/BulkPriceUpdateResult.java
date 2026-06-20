package com.aionn.catalog.application.dto.product.result;

import java.util.List;

public record BulkPriceUpdateResult(
        List<String> affectedProductIds,
        int affectedSkuCount) {
}
