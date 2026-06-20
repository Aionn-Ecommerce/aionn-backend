package com.aionn.catalog.infrastructure.persistence.repository;

import com.aionn.catalog.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, String> {

  @Override
  @EntityGraph(attributePaths = "variants")
  Optional<ProductEntity> findById(String productId);

  @EntityGraph(attributePaths = "variants")
  List<ProductEntity> findByMerchantId(String merchantId, Pageable pageable);

  boolean existsByBrandIdAndStatus(String brandId, String status);

  @Query(value = """
      SELECT DISTINCT p.* FROM products p
        JOIN product_variants v ON v.product_id = p.product_id
        WHERE p.merchant_id = :merchantId AND v.sku_id IN (:skuIds)
      """, nativeQuery = true)
  List<ProductEntity> findByMerchantAndSkuIds(String merchantId, List<String> skuIds);

  @Query(value = """
      SELECT DISTINCT p.* FROM products p
        JOIN product_variants v ON v.product_id = p.product_id
        WHERE v.sku_id IN (:skuIds)
      """, nativeQuery = true)
  List<ProductEntity> findBySkuIds(List<String> skuIds);

  @Query(value = """
      SELECT EXISTS (
        SELECT 1 FROM products p
        WHERE p.category_ids @> jsonb_build_array(CAST(:categoryId AS text))
      )
      """, nativeQuery = true)
  boolean existsByCategoryId(String categoryId);

  @Query(value = """
      SELECT p.* FROM products p
        WHERE p.status = 'PUBLISHED'
        ORDER BY p.updated_at DESC
        LIMIT :limit OFFSET :offset
      """, nativeQuery = true)
  List<ProductEntity> findPublished(int limit, int offset);

  @Query(value = """
      SELECT COUNT(*) FROM products p
        WHERE p.status = 'PUBLISHED'
      """, nativeQuery = true)
  long countPublished();

  @Query(value = """
      SELECT p.* FROM products p
        WHERE p.status = 'PUBLISHED'
          AND (:q IS NULL OR p.name ILIKE CONCAT('%', :q, '%'))
        ORDER BY p.updated_at DESC
        LIMIT :limit OFFSET :offset
      """, nativeQuery = true)
  List<ProductEntity> searchPublished(String q, int limit, int offset);

  @Query(value = """
      SELECT COUNT(*) FROM products p
        WHERE p.status = 'PUBLISHED'
          AND (:q IS NULL OR p.name ILIKE CONCAT('%', :q, '%'))
      """, nativeQuery = true)
  long countSearchPublished(String q);

  @Query(value = """
      SELECT DISTINCT p.* FROM products p
      WHERE p.status = 'PUBLISHED'
        AND p.product_id != :productId
        AND (
          (p.brand_id IS NOT NULL AND :brandId IS NOT NULL AND p.brand_id = :brandId)
          OR
          EXISTS (
            SELECT 1 FROM jsonb_array_elements_text(p.category_ids) cat
            WHERE cat IN (:categoryIds)
          )
        )
      ORDER BY p.updated_at DESC
      LIMIT :limit
      """, nativeQuery = true)
  List<ProductEntity> findRelatedProducts(
      @Param("productId") String productId,
      @Param("brandId") String brandId,
      @Param("categoryIds") List<String> categoryIds,
      @Param("limit") int limit);

  @Query(value = """
      SELECT p.* FROM products p
      LEFT JOIN (
        SELECT r.product_id, AVG(r.rating) as avg_rating, COUNT(r.review_id) as review_count
        FROM product_reviews r
        WHERE r.status = 'VISIBLE'
        GROUP BY r.product_id
      ) rev ON p.product_id = rev.product_id
      WHERE p.status = 'PUBLISHED'
      ORDER BY COALESCE(rev.avg_rating, 0) DESC, COALESCE(rev.review_count, 0) DESC, p.updated_at DESC
      LIMIT :limit
      """, nativeQuery = true)
  List<ProductEntity> findPopularProducts(@Param("limit") int limit);

  @Query(value = """
      SELECT DISTINCT p.* FROM products p
      WHERE p.status = 'PUBLISHED'
        AND (
          (p.brand_id IS NOT NULL AND p.brand_id IN (:brandIds))
          OR
          EXISTS (
            SELECT 1 FROM jsonb_array_elements_text(p.category_ids) cat
            WHERE cat IN (:categoryIds)
          )
        )
      ORDER BY p.updated_at DESC
      LIMIT :limit
      """, nativeQuery = true)
  List<ProductEntity> findPersonalizedProducts(
      @Param("categoryIds") List<String> categoryIds,
      @Param("brandIds") List<String> brandIds,
      @Param("limit") int limit);
}
