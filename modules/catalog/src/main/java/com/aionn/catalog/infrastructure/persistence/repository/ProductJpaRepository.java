package com.aionn.catalog.infrastructure.persistence.repository;

import com.aionn.catalog.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {

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

  /** Cheap existence check used by category deletion. */
  @Query(value = """
      SELECT EXISTS (
        SELECT 1 FROM products p
        WHERE jsonb_exists(p.category_ids, :categoryId)
      )
      """, nativeQuery = true)
  boolean existsByCategoryId(String categoryId);
}

