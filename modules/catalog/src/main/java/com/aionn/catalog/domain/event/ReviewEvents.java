package com.aionn.catalog.domain.event;

import java.time.Instant;

public final class ReviewEvents {

    private ReviewEvents() {
    }

    public record ReviewCreated(
            String reviewId,
            String productId,
            String userId,
            int rating,
            Instant occurredAt) implements CatalogEvent {
        public ReviewCreated(String reviewId, String productId, String userId, int rating) {
            this(reviewId, productId, userId, rating, Instant.now());
        }
    }

    public record ReviewUpdated(
            String reviewId,
            int rating,
            Instant occurredAt) implements CatalogEvent {
        public ReviewUpdated(String reviewId, int rating) {
            this(reviewId, rating, Instant.now());
        }
    }

    public record ReviewHidden(
            String reviewId,
            Instant occurredAt) implements CatalogEvent {
        public ReviewHidden(String reviewId) {
            this(reviewId, Instant.now());
        }
    }

    public record MerchantReplied(
            String reviewId,
            Instant occurredAt) implements CatalogEvent {
        public MerchantReplied(String reviewId) {
            this(reviewId, Instant.now());
        }
    }
}
