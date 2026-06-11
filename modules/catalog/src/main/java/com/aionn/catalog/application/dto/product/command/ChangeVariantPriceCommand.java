package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

import java.math.BigDecimal;

public record ChangeVariantPriceCommand(
                String productId,
                String ownerId,
                String skuId,
                BigDecimal newPrice,
                String currency) implements Command {
}
