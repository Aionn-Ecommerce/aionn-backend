package com.aionn.catalog.application.mapper;

import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.port.out.MerchantPersistencePort;
import com.aionn.catalog.application.port.out.ProductReviewPersistencePort;
import com.aionn.catalog.application.port.out.ProductSoldCounterPersistencePort;
import com.aionn.catalog.domain.model.Merchant;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.model.ProductVariant;
import com.aionn.sharedkernel.integration.port.promotion.FlashSaleQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductResultMapper {

        private final ProductReviewPersistencePort reviewRepository;
        private final ProductSoldCounterPersistencePort soldCounterRepository;
        private final FlashSaleQueryPort flashSaleQueryPort;
        private final MerchantPersistencePort merchantRepository;

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
                long soldCount = soldCounterRepository.getSoldCount(product.getProductId());

                ProductResult.FlashSaleInfo flashSale = buildFlashSale(product.getProductId());

                String provinceCode = null;
                String provinceName = null;
                if (product.getMerchantId() != null) {
                        Merchant merchant = merchantRepository.findById(product.getMerchantId()).orElse(null);
                        if (merchant != null) {
                                provinceCode = merchant.getProvinceCode();
                                provinceName = merchant.getProvinceName();
                        }
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
                                product.getUpdatedAt(),
                                rating,
                                reviewCount,
                                soldCount,
                                flashSale,
                                provinceCode,
                                provinceName);
        }

        private ProductResult.FlashSaleInfo buildFlashSale(String productId) {
                Map<String, FlashSaleQueryPort.ProductFlashSale> active = flashSaleQueryPort
                                .findActiveByProductIds(List.of(productId));
                FlashSaleQueryPort.ProductFlashSale info = active.get(productId);
                if (info == null || info.skuOffers().isEmpty()) {
                        return null;
                }
                List<ProductResult.FlashSaleInfo.SkuOffer> offers = info.skuOffers().stream()
                                .map(o -> new ProductResult.FlashSaleInfo.SkuOffer(
                                                o.skuId(), o.salePrice(), o.currency(),
                                                o.saleStock(), o.soldCount()))
                                .toList();
                String currency = info.skuOffers().get(0).currency();
                return new ProductResult.FlashSaleInfo(
                                info.campaignId(),
                                info.endAt(),
                                info.lowestSalePrice(),
                                currency,
                                info.totalSaleStock(),
                                info.totalSoldCount(),
                                offers);
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
                long soldCount = soldCounterRepository.getSoldCount(product.getProductId());

                Map<String, FlashSaleQueryPort.ProductFlashSale> activeFlash = flashSaleQueryPort
                                .findActiveByProductIds(List.of(product.getProductId()));
                FlashSaleQueryPort.ProductFlashSale fs = activeFlash.get(product.getProductId());
                boolean flashSale = fs != null;
                BigDecimal flashSalePrice = fs == null ? null : fs.lowestSalePrice();
                java.time.Instant flashSaleEndAt = fs == null ? null : fs.endAt();

                // If there is an active flash sale, the customer sees the flash sale price —
                // so priceFrom must reflect that effective lowest price to keep price filters accurate.
                if (flashSalePrice != null && (priceFrom == null || flashSalePrice.compareTo(priceFrom) < 0)) {
                        priceFrom = flashSalePrice;
                }

                // Storefront "onSale" is satisfied by either a variant-level discount
                // (originalPrice > price) OR an active flash sale registration.
                boolean onSale = flashSale || product.variants().stream()
                                .anyMatch(v -> v.originalPrice() != null && v.price() != null
                                                && v.originalPrice().amount().compareTo(v.price().amount()) > 0);

                // Province is sourced from the merchant aggregate (set by the seller in
                // their profile, validated against identity at write time). The code is
                // canonical VN GSO; the name is a denormalized snapshot for display.
                String provinceCode = null;
                String provinceName = null;
                if (product.getMerchantId() != null) {
                        Merchant merchant = merchantRepository.findById(product.getMerchantId()).orElse(null);
                        if (merchant != null) {
                                provinceCode = merchant.getProvinceCode();
                                provinceName = merchant.getProvinceName();
                        }
                }

                // Shipping carrier is no longer modelled at the product level; the
                // storefront-level filter was misleading and BE has no real data.
                List<String> shipping = List.of();

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
                                provinceCode,
                                provinceName,
                                soldCount,
                                flashSale,
                                flashSalePrice,
                                flashSaleEndAt);
        }
}
