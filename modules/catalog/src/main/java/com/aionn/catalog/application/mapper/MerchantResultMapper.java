package com.aionn.catalog.application.mapper;

import com.aionn.catalog.application.dto.merchant.result.MerchantResult;
import com.aionn.catalog.domain.model.Merchant;
import org.springframework.stereotype.Component;

@Component
public class MerchantResultMapper {

    public MerchantResult toResult(Merchant merchant) {
        return new MerchantResult(
                merchant.getMerchantId(),
                merchant.getOwnerId(),
                merchant.getName(),
                merchant.getLogoUrl(),
                merchant.getDescription(),
                merchant.getStatus().name(),
                merchant.getCreatedAt(),
                merchant.getUpdatedAt());
    }
}

