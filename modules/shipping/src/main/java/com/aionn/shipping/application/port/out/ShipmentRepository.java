package com.aionn.shipping.application.port.out;

import com.aionn.shipping.domain.model.Shipment;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository {

    Shipment save(Shipment shipment);

    Optional<Shipment> findById(String shipmentId);

    Optional<Shipment> findByTrackingCode(String trackingCode);

    List<Shipment> findByOrderId(String orderId);
}

