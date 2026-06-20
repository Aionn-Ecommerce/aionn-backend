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
                .provinceCode(merchant.getProvinceCode())
                .provinceName(merchant.getProvinceName())
                .status(merchant.getStatus().name())
                .commissionRate(merchant.getCommissionRate())
                .stripeAccountId(merchant.getStripeAccountId())
                .stripeChargesEnabled(merchant.isStripeChargesEnabled())
                .stripePayoutsEnabled(merchant.isStripePayoutsEnabled())
                .build();
    }

    public Merchant toDomain(MerchantEntity entity) {
        return new Merchant(
                entity.getMerchantId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getLogoUrl(),
                entity.getDescription(),
                entity.getProvinceCode(),
                entity.getProvinceName(),
                MerchantStatus.valueOf(entity.getStatus()),
                entity.getCommissionRate(),
                entity.getStripeAccountId(),
                entity.isStripeChargesEnabled(),
                entity.isStripePayoutsEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
