package com.aionn.catalog.application.mapper;

import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.model.ProductVariant;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ProductResultMapper {

    public ProductResult toResult(Product product) {
        List<ProductResult.VariantResult> variants = product.variants().stream()
                .map(v -> new ProductResult.VariantResult(
                        v.skuId(),
                        v.attributeValues(),
                        v.price() == null ? null : v.price().amount(),
                        v.price() == null ? null : v.price().currency()))
                .toList();
        return new ProductResult(
                product.getProductId(),
                product.getMerchantId(),
                product.getName(),
                product.getBrandId(),
                product.categoryIds(),
                product.imageList(),
                product.tags(),
                product.collectionIds(),
                product.attributes(),
                variants,
                product.getAiDescription(),
                product.getStatus().name(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    /**
     * Build the search document for a product. Only filterable attributes
     * (decided by the AttributeTemplate per category) end up in
     * {@code filterableAttributes}; the application service computes that map
     * before calling this method.
     */
    public ProductSearchDocument toSearchDocument(Product product, Map<String, String> filterableAttributes) {
        List<BigDecimal> prices = product.variants().stream()
                .map(ProductVariant::price)
                .filter(p -> p != null)
                .map(price -> price.amount())
                .toList();

        BigDecimal priceFrom = prices.isEmpty() ? null : prices.stream().reduce(BigDecimal::min).orElse(null);
        BigDecimal priceTo = prices.isEmpty() ? null : prices.stream().reduce(BigDecimal::max).orElse(null);
        String currency = product.variants().stream()
                .map(ProductVariant::price)
                .filter(p -> p != null)
                .map(price -> price.currency())
                .findFirst()
                .orElse(null);

        return new ProductSearchDocument(
                product.getProductId(),
                product.getMerchantId(),
                product.getName(),
                product.getAiDescription(),
                product.getBrandId(),
                product.categoryIds(),
                product.collectionIds(),
                product.tags(),
                product.imageList(),
                filterableAttributes,
                priceFrom,
                priceTo,
                currency,
                product.getStatus().name(),
                product.getUpdatedAt());
    }
}

