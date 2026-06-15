package com.aionn.catalog.application.mapper;

import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.port.out.ProductReviewPersistencePort;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.model.ProductVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductResultMapper {

        private final ProductReviewPersistencePort reviewRepository;

        public ProductResult toResult(Product product) {
                List<ProductResult.VariantResult> variants = product.variants().stream()
                                .map(v -> new ProductResult.VariantResult(
                                                v.skuId(),
                                                v.attributeValues(),
                                                v.price() == null ? null : v.price().amount(),
                                                v.originalPrice() == null ? null : v.originalPrice().amount(),
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

                Double avgRating = reviewRepository.getAverageRating(product.getProductId());
                double rating = avgRating == null ? 0.0 : avgRating;
                long reviewCount = reviewRepository.countVisibleReviews(product.getProductId());

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
                                product.getUpdatedAt(),
                                rating,
                                reviewCount);
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

                Double avgRating = reviewRepository.getAverageRating(product.getProductId());
                double rating = avgRating == null ? 0.0 : avgRating;

                boolean onSale = product.variants().stream()
                                .anyMatch(v -> v.originalPrice() != null && v.price() != null
                                                && v.originalPrice().amount().compareTo(v.price().amount()) > 0);

                String location = "Đà Nẵng";
                if (product.getMerchantId() != null) {
                        switch (product.getMerchantId()) {
                                case "MER_001":
                                case "MER_002":
                                case "MER_003":
                                case "MER_004":
                                case "MER_005":
                                        location = "Hà Nội";
                                        break;
                                case "MER_006":
                                case "MER_007":
                                case "MER_008":
                                case "MER_009":
                                case "MER_010":
                                        location = "TP. Hồ Chí Minh";
                                        break;
                        }
                }

                List<String> shipping = new ArrayList<>();
                shipping.add("GHN");
                int idVal = 0;
                if (product.getMerchantId() != null) {
                        try {
                                idVal = Integer.parseInt(product.getMerchantId().replaceAll("\\D+", ""));
                        } catch (Exception e) {
                        }
                }
                if (idVal <= 8) {
                        shipping.add("GHTK");
                }
                if (idVal >= 5) {
                        shipping.add("J&T");
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
                                product.getUpdatedAt(),
                                rating,
                                onSale,
                                shipping,
                                location);
        }
}
