package com.aionn.catalog.application.dto.review.result;

import java.util.Map;

public record RatingSummary(
        double averageRating,
        long totalReviews,
        Map<Integer, Long> ratingDistribution) {
}
