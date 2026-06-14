package com.aionn.ucp.application.port.out;

import java.util.Optional;

public interface OrderingQueryPort {

    Optional<OrderSnapshot> findOrderById(String orderId);

    record OrderSnapshot(
            String id,
            String userId,
            String merchantId,
            String currency,
            String status,
            long subtotalMinor,
            long shippingMinor,
            long totalMinor,
            java.util.List<Line> lines,
            String permalinkUrl) {

        public record Line(String skuId, int qty, long unitPriceMinor, long lineTotalMinor) {
        }
    }
}
