package com.aionn.catalog.application.port.out;

import com.aionn.catalog.domain.model.Product;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String productId);

    List<Product> findByMerchant(String merchantId, OffsetPagination pagination);

    /** Used by bulk price update to load only requested SKUs by merchant. */
    List<Product> findByMerchantAndSkuIds(String merchantId, List<String> skuIds);
}

