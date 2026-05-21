package com.aionn.shipping.infrastructure.adapter;

import com.aionn.shipping.application.port.out.ShippingRateRepository;
import com.aionn.shipping.domain.model.ShippingRate;
import com.aionn.shipping.infrastructure.persistence.entity.ShippingRateEntity;
import com.aionn.shipping.infrastructure.persistence.mapper.ShippingRateDomainMapper;
import com.aionn.shipping.infrastructure.persistence.repository.ShippingRateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShippingRateRepositoryAdapter implements ShippingRateRepository {

    private final ShippingRateJpaRepository jpa;
    private final ShippingRateDomainMapper mapper;

    @Override
    public ShippingRate save(ShippingRate rate) {
        ShippingRateEntity existing = jpa.findById(rate.getRateId()).orElse(null);
        return mapper.toDomain(jpa.save(mapper.toEntity(rate, existing)));
    }

    @Override
    public Optional<ShippingRate> findById(String rateId) {
        return jpa.findById(rateId).map(mapper::toDomain);
    }

    @Override
    public Optional<ShippingRate> findByZoneCode(String zoneCode) {
        return jpa.findByZoneCode(zoneCode).map(mapper::toDomain);
    }
}

