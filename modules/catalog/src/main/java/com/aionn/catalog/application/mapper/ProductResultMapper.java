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

                java.util.Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale();
                String name = product.getName();
                String aiDescription = product.getAiDescription();

                Product.Translation trans = product.translations().stream()
                                .filter(t -> t.locale().equalsIgnoreCase(locale.getLanguage()))
                                .findFirst()
                                .orElse(null);
                if (trans != null) {
                        name = trans.name();
                        aiDescription = trans.aiDescription();
                }

                return new ProductResult(
                                product.getProductId(),
                                product.getMerchantId(),
                                name,
                                product.getBrandId(),
                                product.categoryIds(),
                                product.imageList(),
                                product.tags(),
                                product.collectionIds(),
                                product.attributes(),
                                variants,
                                aiDescription,
                                product.getStatus().name(),
                                product.getCreatedAt(),
                                product.getUpdatedAt());
        }

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

                java.util.Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale();
                String name = product.getName();
                String aiDescription = product.getAiDescription();

                Product.Translation trans = product.translations().stream()
                                .filter(t -> t.locale().equalsIgnoreCase(locale.getLanguage()))
                                .findFirst()
                                .orElse(null);
                if (trans != null) {
                        name = trans.name();
                        aiDescription = trans.aiDescription();
                }

                return new ProductSearchDocument(
                                product.getProductId(),
                                product.getMerchantId(),
                                name,
                                aiDescription,
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
