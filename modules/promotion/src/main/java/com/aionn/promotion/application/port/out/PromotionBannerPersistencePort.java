package com.aionn.promotion.application.port.out;

import com.aionn.promotion.domain.model.PromotionBanner;

import java.util.List;

public interface PromotionBannerPersistencePort {

    List<PromotionBanner> findAllActive();
}
