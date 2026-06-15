package com.aionn.catalog.adapter.rest.dto.product;

import com.aionn.catalog.application.dto.product.command.BulkPriceUpdateCommand;
import com.aionn.catalog.application.policy.CatalogValidationConstants;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record BulkPriceUpdateRequest(
        @NotEmpty @Size(max = CatalogValidationConstants.BULK_PRICE_UPDATE_MAX_SIZE) List<String> skuIds,
        @NotNull BulkPriceUpdateCommand.ChangeType changeType,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal value,
        @Size(min = 3, max = 3) String currency) {
}
