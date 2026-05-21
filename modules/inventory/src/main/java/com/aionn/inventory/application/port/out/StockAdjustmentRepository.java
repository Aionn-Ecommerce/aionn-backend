package com.aionn.inventory.application.port.out;

import com.aionn.inventory.domain.model.StockAdjustment;

public interface StockAdjustmentRepository {

    StockAdjustment save(StockAdjustment adjustment);
}

