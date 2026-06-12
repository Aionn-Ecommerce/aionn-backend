package com.aionn.catalog.infrastructure.persistence.adapter.product;

import com.aionn.catalog.application.port.out.ProductRepository;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.infrastructure.persistence.mapper.ProductDomainMapper;
import com.aionn.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpa;
    private final ProductDomainMapper mapper;

    @Override
    public Product save(Product product) {
        return mapper.toDomain(jpa.save(mapper.toEntity(product)));
    }

    @Override
    public Optional<Product> findById(String productId) {
        return jpa.findById(productId).map(mapper::toDomain);
    }

    @Override
    public List<Product> findByMerchant(String merchantId, OffsetPagination pagination) {
        return jpa.findByMerchantId(merchantId, PageRequest.of(pagination.page(), pagination.size())).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByMerchantAndSkuIds(String merchantId, List<String> skuIds) {
        return jpa.findByMerchantAndSkuIds(merchantId, skuIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findBySkuIds(List<String> skuIds) {
        return jpa.findBySkuIds(skuIds).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
