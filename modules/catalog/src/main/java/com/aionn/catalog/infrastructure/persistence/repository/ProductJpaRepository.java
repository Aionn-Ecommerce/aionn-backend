package com.aionn.catalog.infrastructure.persistence.repository;

import com.aionn.catalog.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {

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
}
