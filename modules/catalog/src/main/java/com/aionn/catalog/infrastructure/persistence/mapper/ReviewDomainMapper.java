package com.aionn.catalog.infrastructure.persistence.mapper;

import com.aionn.catalog.domain.model.ProductReview;
import com.aionn.catalog.domain.valueobject.ReviewStatus;
import com.aionn.catalog.infrastructure.persistence.entity.ProductReviewEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ReviewDomainMapper {

    public ProductReviewEntity toEntity(ProductReview domain) {
        if (domain == null) {
            return null;
        }
        return ProductReviewEntity.builder()
                .reviewId(domain.getReviewId())
                .productId(domain.getProductId())
                .userId(domain.getUserId())
                .orderId(domain.getOrderId())
                .rating(domain.getRating())
                .title(domain.getTitle())
                .content(domain.getContent())
                .imageUrls(domain.getImageUrls() != null ? new ArrayList<>(domain.getImageUrls()) : new ArrayList<>())
                .status(domain.getStatus().name())
                .merchantReply(domain.getMerchantReply())
                .merchantRepliedAt(domain.getMerchantRepliedAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public ProductReview toDomain(ProductReviewEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ProductReview(
                entity.getReviewId(),
                entity.getProductId(),
                entity.getUserId(),
                entity.getOrderId(),
                entity.getRating(),
                entity.getTitle(),
                entity.getContent(),
                entity.getImageUrls() != null ? new ArrayList<>(entity.getImageUrls()) : new ArrayList<>(),
                entity.getStatus() != null ? ReviewStatus.valueOf(entity.getStatus()) : ReviewStatus.VISIBLE,
                entity.getMerchantReply(),
                entity.getMerchantRepliedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
