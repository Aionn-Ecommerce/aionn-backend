package com.aionn.ordering.application.dto.order.command;

import com.aionn.ordering.domain.valueobject.ShippingAddress;
import com.aionn.sharedkernel.application.command.Command;

import java.math.BigDecimal;

public final class OrderCommands {

        private OrderCommands() {
        }

        public record PlaceOrder(
                        String userId,
                        String addressId,
                        String paymentMethodId,
                        String currency,
                        BigDecimal shippingFee,
                        ShippingAddress shippingAddressSnapshot) implements Command {
        }

        public record ConfirmPreparation(String orderId, String merchantId) implements Command {
        }

        public record CancelOrder(String orderId, String userId, String reason) implements Command {
        }

        public record RejectOrder(String orderId, String merchantId, String reason) implements Command {
        }

        public record ChangeShippingInfo(
                        String orderId,
                        String userId,
                        ShippingAddress newAddress,
                        BigDecimal newShippingFee) implements Command {
        }

        public record ConfirmShipped(String orderId, String shipmentId) implements Command {
        }

        public record ConfirmDelivered(String orderId) implements Command {
        }
}
