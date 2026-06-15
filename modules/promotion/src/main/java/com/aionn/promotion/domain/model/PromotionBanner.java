package com.aionn.promotion.domain.model;

import lombok.Getter;
import java.time.Instant;

@Getter
public class PromotionBanner {

    private final String bannerId;
    private final String title;
    private final String imageUrl;
    private final String linkUrl;
    private final int displayOrder;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PromotionBanner(String bannerId, String title, String imageUrl, String linkUrl,
            int displayOrder, boolean active, Instant createdAt, Instant updatedAt) {
        this.bannerId = bannerId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.displayOrder = displayOrder;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
