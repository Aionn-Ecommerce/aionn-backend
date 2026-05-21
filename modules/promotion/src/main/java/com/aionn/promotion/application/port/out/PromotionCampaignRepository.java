package com.aionn.promotion.application.port.out;

import com.aionn.promotion.domain.model.PromotionCampaign;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PromotionCampaignRepository {

    PromotionCampaign save(PromotionCampaign campaign);

    Optional<PromotionCampaign> findById(String campaignId);

    /** UC9.1 sweep - SCHEDULED whose startDate <= now -> RUNNING. */
    List<PromotionCampaign> findToActivate(Instant now, int limit);

    /** UC9.1 sweep - RUNNING whose endDate <= now -> ENDED. */
    List<PromotionCampaign> findToEnd(Instant now, int limit);
}

