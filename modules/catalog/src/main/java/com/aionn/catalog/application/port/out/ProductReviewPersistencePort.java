package com.aionn.catalog.application.port.out;

import com.aionn.catalog.domain.model.ProductReview;
import com.aionn.catalog.domain.valueobject.ReviewStatus;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductReviewPersistencePort {

    ProductReview save(ProductReview review);

    Optional<ProductReview> findById(String reviewId);

    boolean existsByUserIdAndProductId(String userId, String productId);

    List<ProductReview> findByProductIdAndStatus(String productId, ReviewStatus status, OffsetPagination pagination);

    List<ProductReview> findByUserId(String userId, OffsetPagination pagination);

    long countByUserId(String userId);

    void deleteById(String reviewId);

    Map<Integer, Long> getRatingDistribution(String productId);

    Double getAverageRating(String productId);

    long countVisibleReviews(String productId);
}
