package com.aionn.promotion.infrastructure.persistence.adapter;

import com.aionn.promotion.application.port.out.PromotionBannerPersistencePort;
import com.aionn.promotion.domain.model.PromotionBanner;
import com.aionn.promotion.infrastructure.persistence.mapper.PromotionBannerDomainMapper;
import com.aionn.promotion.infrastructure.persistence.repository.JpaPromotionBannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PromotionBannerPersistenceAdapter implements PromotionBannerPersistencePort {

    private final JpaPromotionBannerRepository jpa;
    private final PromotionBannerDomainMapper mapper;

    @Override
    public List<PromotionBanner> findAllActive() {
        return jpa.findAllActiveOrderByDisplayOrder().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
