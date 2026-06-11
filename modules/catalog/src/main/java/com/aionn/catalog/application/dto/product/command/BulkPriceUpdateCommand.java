package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

import java.math.BigDecimal;
import java.util.List;

public record BulkPriceUpdateCommand(
        String ownerId,
        List<String> skuIds,
        ChangeType changeType,
        BigDecimal value,
        String currency) implements Command {

    public enum ChangeType {
        SET, INCREASE_AMOUNT, DECREASE_AMOUNT, INCREASE_PERCENT, DECREASE_PERCENT
    }
}
