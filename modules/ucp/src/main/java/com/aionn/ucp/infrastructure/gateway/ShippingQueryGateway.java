package com.aionn.ucp.infrastructure.gateway;

import com.aionn.ucp.application.port.out.ShippingQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingQueryGateway implements ShippingQueryPort {

    private static final long FREE_SHIPPING_THRESHOLD_VND = 500_000L; // 500k VND

    @Override
    public List<ShippingOption> getShippingOptions(String addressRegion, String addressCountry,
            long orderTotalMinor, String currency) {
        List<ShippingOption> options = new ArrayList<>();

        // Standard shipping
        long standardFee = computeStandardFee(addressCountry, orderTotalMinor);
        options.add(new ShippingOption(
                "standard",
                "Standard Shipping",
                "Arrives in 5-8 business days",
                standardFee,
                currency != null ? currency : "VND"));

        // Express shipping
        long expressFee = computeExpressFee(addressCountry, orderTotalMinor);
        options.add(new ShippingOption(
                "express",
                "Express Shipping",
                "Arrives in 2-3 business days",
                expressFee,
                currency != null ? currency : "VND"));

        return options;
    }

    private long computeStandardFee(String country, long orderTotalMinor) {
        // Free standard shipping for large orders
        if (orderTotalMinor >= FREE_SHIPPING_THRESHOLD_VND) {
            return 0;
        }
        // Domestic (VN) vs international
        if ("VN".equalsIgnoreCase(country) || "vn".equals(country)) {
            return 30_000; // 30,000 VND
        }
        return 150_000; // 150,000 VND for international
    }

    private long computeExpressFee(String country, long orderTotalMinor) {
        if ("VN".equalsIgnoreCase(country) || "vn".equals(country)) {
            return 50_000; // 50,000 VND
        }
        return 300_000; // 300,000 VND for international
    }
}
