package com.aionn.catalog.infrastructure.persistence.repository;

import com.aionn.catalog.infrastructure.persistence.entity.ProductReviewEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReviewEntity, String> {

    boolean existsByUserIdAndProductId(String userId, String productId);

    long countByUserId(String userId);

    List<ProductReviewEntity> findByProductIdAndStatusOrderByCreatedAtDesc(String productId, String status,
            Pageable pageable);

    List<ProductReviewEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT r.rating, COUNT(r) FROM ProductReviewEntity r WHERE r.productId = :productId AND r.status = 'VISIBLE' GROUP BY r.rating")
    List<Object[]> countRatingsGroupByRating(String productId);

    @Query("SELECT AVG(r.rating) FROM ProductReviewEntity r WHERE r.productId = :productId AND r.status = 'VISIBLE'")
    Double getAverageRating(String productId);

    @Query("SELECT COUNT(r) FROM ProductReviewEntity r WHERE r.productId = :productId AND r.status = 'VISIBLE'")
    long countVisibleReviews(String productId);
}
