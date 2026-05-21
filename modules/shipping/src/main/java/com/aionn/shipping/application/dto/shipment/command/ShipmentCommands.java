package com.aionn.shipping.application.dto.shipment.command;

import com.aionn.shipping.domain.valueobject.ShipmentAddress;
import com.aionn.shipping.domain.valueobject.ShipmentDimensions;
import com.aionn.sharedkernel.application.command.Command;

import java.math.BigDecimal;

public final class ShipmentCommands {

        private ShipmentCommands() {
        }

        public record CreateShipment(
                        String orderId,
                        ShipmentAddress address,
                        ShipmentDimensions dimensions,
                        BigDecimal codAmount,
                        BigDecimal shippingFee,
                        String currency) implements Command {
        }

        public record QuoteShipping(
                        String orderId,
                        ShipmentAddress address,
                        ShipmentDimensions dimensions,
                        String currency) implements Command {
        }

        public record FetchLabel(String shipmentId, String merchantId) implements Command {
        }

        public record ResolveIssue(String shipmentId, String issueType, String resolution) implements Command {
        }

        public record CancelShipment(String shipmentId, String reason) implements Command {
        }

        public record CarrierWebhook(
                        String trackingCode,
                        String type,
                        String currentLocation,
                        String statusDesc,
                        String shipperName,
                        String shipperPhone,
                        String signatureUrl,
                        String reason,
                        String warehouseId) implements Command {
        }
}
