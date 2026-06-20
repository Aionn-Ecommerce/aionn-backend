package com.aionn.ucp.application.port.out;

import java.math.BigDecimal;
import java.util.List;

public interface OrderPlacementPort {

    PlacedOrder place(PlaceCommand command);

    record PlaceCommand(
            String userId,
            List<Line> lines,
            String voucherCode,
            String paymentMethodId,
            String addressId,
            String currency,
            BigDecimal shippingFee) {

        public record Line(String skuId, int qty) {
        }
    }

    record PlacedOrder(String orderId, long totalMinor, String currency) {
    }
}
