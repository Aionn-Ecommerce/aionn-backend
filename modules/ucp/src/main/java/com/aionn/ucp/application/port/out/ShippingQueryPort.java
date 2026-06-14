package com.aionn.ucp.application.port.out;

import java.util.List;

public interface ShippingQueryPort {

    List<ShippingOption> getShippingOptions(String addressRegion, String addressCountry, long orderTotalMinor,
            String currency);

    record ShippingOption(
            String id,
            String title,
            String description,
            long feeMinor,
            String currency) {
    }
}
