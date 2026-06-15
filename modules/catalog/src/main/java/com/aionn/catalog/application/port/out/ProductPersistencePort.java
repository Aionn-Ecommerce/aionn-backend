package com.aionn.catalog.application.port.out;

import com.aionn.catalog.domain.model.Product;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;

import java.util.List;
import java.util.Optional;

public interface ProductPersistencePort {

    Product save(Product product);

    Optional<Product> findById(String productId);

    List<Product> findByMerchant(String merchantId, OffsetPagination pagination);

    List<Product> findByMerchantAndSkuIds(String merchantId, List<String> skuIds);

    List<Product> findBySkuIds(List<String> skuIds);

    List<Product> findPublished(int limit, int offset);

    List<Product> searchPublished(String queryOrNull, int limit);

    List<Product> findRelatedProducts(String productId, String brandId, List<String> categoryIds, int limit);

    List<Product> findPopularProducts(int limit);

    List<Product> findPersonalizedProducts(List<String> categoryIds, List<String> brandIds, int limit);

    /**
     * Hydrate a list of product ids in the order supplied. Used by the
     * OpenSearch-backed search to preserve relevance ranking after the
     * domain entities are loaded from JPA.
     */
    List<Product> findByIdsPreserveOrder(List<String> productIds);
}
