package com.aionn.catalog.infrastructure.persistence.mapper;

import com.aionn.catalog.domain.model.Merchant;
import com.aionn.catalog.domain.valueobject.MerchantStatus;
import com.aionn.catalog.infrastructure.persistence.entity.MerchantEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantDomainMapper {

    public MerchantEntity toEntity(Merchant merchant) {
        return MerchantEntity.builder()
                .merchantId(merchant.getMerchantId())
                .ownerId(merchant.getOwnerId())
                .name(merchant.getName())
                .logoUrl(merchant.getLogoUrl())
                .description(merchant.getDescription())
                .status(merchant.getStatus().name())
                .build();
    }

    public Merchant toDomain(MerchantEntity entity) {
        return new Merchant(
                entity.getMerchantId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getLogoUrl(),
                entity.getDescription(),
                MerchantStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}

