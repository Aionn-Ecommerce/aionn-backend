package com.aionn.catalog.application.port.out.product;

import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(String productId);

    List<Product> findByMerchant(String merchantId, OffsetPagination pagination);

    List<Product> findByMerchantAndStatus(String merchantId, ProductStatus status, OffsetPagination pagination);

    List<Product> findByStatus(ProductStatus status, OffsetPagination pagination);

    List<Product> findByMerchantAndSkuIds(String merchantId, List<String> skuIds);
}
