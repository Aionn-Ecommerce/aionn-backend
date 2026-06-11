package com.aionn.catalog.infrastructure.persistence.repository;

import com.aionn.catalog.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {

  /** Eagerly fetch variants so the domain mapper can run after the tx closes. */
  @Override
  @EntityGraph(attributePaths = "variants")
  Optional<ProductEntity> findById(String productId);

  @EntityGraph(attributePaths = "variants")
  List<ProductEntity> findByMerchantId(String merchantId, Pageable pageable);

  boolean existsByBrandIdAndStatus(String brandId, String status);

  /**
   * Returns the products that own at least one of the supplied SKUs and
   * belong to the merchant. Used by bulk price update.
   */
  @Query(value = """
      SELECT DISTINCT p.* FROM products p
        JOIN product_variants v ON v.product_id = p.product_id
        WHERE p.merchant_id = :merchantId AND v.sku_id IN (:skuIds)
      """, nativeQuery = true)
  List<ProductEntity> findByMerchantAndSkuIds(String merchantId, List<String> skuIds);

  /**
   * Cheap existence check used by category deletion. Uses the {@code @>}
   * containment form so it can hit the {@code jsonb_path_ops} GIN index.
   */
  @Query(value = """
      SELECT EXISTS (
        SELECT 1 FROM products p
        WHERE p.category_ids @> jsonb_build_array(CAST(:categoryId AS text))
      )
      """, nativeQuery = true)
  boolean existsByCategoryId(String categoryId);
}
