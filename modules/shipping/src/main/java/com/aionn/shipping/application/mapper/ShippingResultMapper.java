package com.aionn.shipping.application.mapper;

import com.aionn.shipping.application.dto.rate.result.ShippingRateResult;
import com.aionn.shipping.application.dto.shipment.result.ShipmentResult;
import com.aionn.shipping.domain.model.Shipment;
import com.aionn.shipping.domain.model.ShippingRate;
import org.springframework.stereotype.Component;

@Component
public class ShippingResultMapper {

    public ShipmentResult toResult(Shipment s) {
        return new ShipmentResult(
                s.getShipmentId(),
                s.getOrderId(),
                s.getTrackingCode(),
                s.getCarrierOrderId(),
                s.getLabelUrl(),
                s.getCodAmount(),
                s.getShippingFee(),
                s.getCurrency(),
                s.getStatus().name(),
                s.getCurrentLocation(),
                s.getShipperName(),
                s.getShipperPhone(),
                s.getAttemptCount(),
                s.getLastFailureReason(),
                s.getExpectedDeliveryDate(),
                s.getPickedAt(),
                s.getDeliveredAt(),
                s.getCancelledAt(),
                s.getReturnedAt(),
                s.getCreatedAt(),
                s.getUpdatedAt());
    }

    public ShippingRateResult toResult(ShippingRate r) {
        return new ShippingRateResult(
                r.getRateId(),
                r.getZoneCode(),
                r.getBaseFee(),
                r.getCurrency(),
                r.getCondition(),
                r.getCreatedAt(),
                r.getUpdatedAt());
    }
}

