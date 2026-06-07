package com.aionn.shipping.domain.valueobject;

import java.math.BigDecimal;

public record ShipmentDimensions(
        int weightGram,
        BigDecimal lengthCm,
        BigDecimal widthCm,
        BigDecimal heightCm) {

    public ShipmentDimensions {
        if (weightGram < 0)
            throw new IllegalArgumentException("weight must be >= 0");
    }
}

