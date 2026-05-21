package com.aionn.catalog.infrastructure.persistence.mapper;

import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.model.ProductVariant;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.catalog.infrastructure.persistence.entity.ProductEntity;
import com.aionn.catalog.infrastructure.persistence.entity.ProductVariantEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ProductDomainMapper {

    public ProductEntity toEntity(Product product) {
        ProductEntity entity = ProductEntity.builder()
                .productId(product.getProductId())
                .merchantId(product.getMerchantId())
                .brandId(product.getBrandId())
                .name(product.getName())
                .categoryIds(new ArrayList<>(product.categoryIds()))
                .imageList(new ArrayList<>(product.imageList()))
                .tags(new ArrayList<>(product.tags()))
                .collectionIds(new ArrayList<>(product.collectionIds()))
                .attributes(new HashMap<>(product.attributes()))
                .aiDescription(product.getAiDescription())
                .status(product.getStatus().name())
                .build();
        List<ProductVariantEntity> variantEntities = new ArrayList<>();
        for (ProductVariant variant : product.variants()) {
            ProductVariantEntity ve = ProductVariantEntity.builder()
                    .skuId(variant.skuId())
                    .product(entity)
                    .attributeValues(new HashMap<>(variant.attributeValues()))
                    .price(Optional.ofNullable(variant.price()).map(Money::amount).orElse(null))
                    .currency(Optional.ofNullable(variant.price()).map(Money::currency).orElse(null))
                    .build();
            variantEntities.add(ve);
        }
        entity.setVariants(variantEntities);
        return entity;
    }

    public Product toDomain(ProductEntity entity) {
        List<ProductVariant> variants = new ArrayList<>();
        if (entity.getVariants() != null) {
            for (ProductVariantEntity ve : entity.getVariants()) {
                Money price = ve.getPrice() != null && ve.getCurrency() != null
                        ? Money.of(ve.getPrice(), ve.getCurrency())
                        : null;
                variants.add(new ProductVariant(ve.getSkuId(),
                        ve.getAttributeValues() != null ? ve.getAttributeValues() : Map.of(),
                        price));
            }
        }
        return new Product(
                entity.getProductId(),
                entity.getMerchantId(),
                entity.getName(),
                entity.getBrandId(),
                entity.getCategoryIds() != null ? entity.getCategoryIds() : List.of(),
                entity.getImageList() != null ? entity.getImageList() : List.of(),
                entity.getTags() != null ? entity.getTags() : List.of(),
                entity.getCollectionIds() != null ? entity.getCollectionIds() : List.of(),
                entity.getAttributes() != null ? entity.getAttributes() : Map.of(),
                variants,
                entity.getAiDescription(),
                ProductStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}

