package com.aionn.catalog.infrastructure.persistence.adapter.review;

import com.aionn.catalog.application.port.out.ProductReviewPersistencePort;
import com.aionn.catalog.domain.model.ProductReview;
import com.aionn.catalog.domain.valueobject.ReviewStatus;
import com.aionn.catalog.infrastructure.persistence.entity.ProductReviewEntity;
import com.aionn.catalog.infrastructure.persistence.mapper.ReviewDomainMapper;
import com.aionn.catalog.infrastructure.persistence.repository.ProductReviewRepository;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductReviewPersistenceAdapter implements ProductReviewPersistencePort {

    private final ProductReviewRepository jpa;
    private final ReviewDomainMapper mapper;

    @Override
    public ProductReview save(ProductReview review) {
        ProductReviewEntity entity = mapper.toEntity(review);
        ProductReviewEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ProductReview> findById(String reviewId) {
        return jpa.findById(reviewId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserIdAndProductId(String userId, String productId) {
        return jpa.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<ProductReview> findByProductIdAndStatus(String productId, ReviewStatus status, OffsetPagination pagination) {
        PageRequest pageRequest = PageRequest.of(pagination.page(), pagination.size());
        return jpa.findByProductIdAndStatusOrderByCreatedAtDesc(productId, status.name(), pageRequest).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ProductReview> findByUserId(String userId, OffsetPagination pagination) {
        PageRequest pageRequest = PageRequest.of(pagination.page(), pagination.size());
        return jpa.findByUserIdOrderByCreatedAtDesc(userId, pageRequest).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Map<Integer, Long> getRatingDistribution(String productId) {
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        List<Object[]> rows = jpa.countRatingsGroupByRating(productId);
        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null && row.length >= 2) {
                    Integer rating = ((Number) row[0]).intValue();
                    Long count = ((Number) row[1]).longValue();
                    distribution.put(rating, count);
                }
            }
        }
        return distribution;
    }

    @Override
    public long countByUserId(String userId) {
        return jpa.countByUserId(userId);
    }

    @Override
    public void deleteById(String reviewId) {
        jpa.deleteById(reviewId);
    }

    @Override
    public Double getAverageRating(String productId) {
        Double avg = jpa.getAverageRating(productId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public long countVisibleReviews(String productId) {
        return jpa.countVisibleReviews(productId);
    }
}
