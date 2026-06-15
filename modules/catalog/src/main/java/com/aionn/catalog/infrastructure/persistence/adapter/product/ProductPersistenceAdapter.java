package com.aionn.catalog.infrastructure.persistence.adapter.product;

import com.aionn.catalog.application.port.out.ProductPersistencePort;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.infrastructure.persistence.mapper.ProductDomainMapper;
import com.aionn.catalog.infrastructure.persistence.repository.ProductRepository;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductPersistencePort {

    private final ProductRepository jpa;
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

    @Override
    public List<Product> findPublished(int limit, int offset) {
        return jpa.findPublished(limit, offset).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> searchPublished(String queryOrNull, int limit) {
        String q = (queryOrNull == null || queryOrNull.isBlank()) ? null : queryOrNull.trim();
        return jpa.searchPublished(q, Math.max(1, limit)).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findRelatedProducts(String productId, String brandId, List<String> categoryIds, int limit) {
        List<String> safeCategoryIds = (categoryIds == null || categoryIds.isEmpty()) ? List.of("") : categoryIds;
        return jpa.findRelatedProducts(productId, brandId, safeCategoryIds, limit).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findPopularProducts(int limit) {
        return jpa.findPopularProducts(limit).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findPersonalizedProducts(List<String> categoryIds, List<String> brandIds, int limit) {
        List<String> safeCategoryIds = (categoryIds == null || categoryIds.isEmpty()) ? List.of("") : categoryIds;
        List<String> safeBrandIds = (brandIds == null || brandIds.isEmpty()) ? List.of("") : brandIds;
        return jpa.findPersonalizedProducts(safeCategoryIds, safeBrandIds, limit).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByIdsPreserveOrder(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        java.util.Map<String, Product> byId = jpa.findAllById(productIds).stream()
                .map(mapper::toDomain)
                .collect(java.util.stream.Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));
        return productIds.stream()
                .map(byId::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
