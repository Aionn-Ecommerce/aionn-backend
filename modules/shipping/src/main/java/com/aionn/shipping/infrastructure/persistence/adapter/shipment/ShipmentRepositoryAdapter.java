package com.aionn.shipping.infrastructure.persistence.adapter.shipment;

import com.aionn.shipping.application.port.out.ShipmentRepository;
import com.aionn.shipping.domain.model.Shipment;
import com.aionn.shipping.infrastructure.persistence.entity.ShipmentEntity;
import com.aionn.shipping.infrastructure.persistence.mapper.ShipmentDomainMapper;
import com.aionn.shipping.infrastructure.persistence.repository.ShipmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShipmentRepositoryAdapter implements ShipmentRepository {

    private final ShipmentJpaRepository jpa;
    private final ShipmentDomainMapper mapper;

    @Override
    public Shipment save(Shipment shipment) {
        ShipmentEntity existing = jpa.findById(shipment.getShipmentId()).orElse(null);
        return mapper.toDomain(jpa.save(mapper.toEntity(shipment, existing)));
    }

    @Override
    public Optional<Shipment> findById(String shipmentId) {
        return jpa.findById(shipmentId).map(mapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByTrackingCode(String trackingCode) {
        return jpa.findByTrackingCode(trackingCode).map(mapper::toDomain);
    }

    @Override
    public List<Shipment> findByOrderId(String orderId) {
        return jpa.findByOrderId(orderId).stream().map(mapper::toDomain).toList();
    }
}

