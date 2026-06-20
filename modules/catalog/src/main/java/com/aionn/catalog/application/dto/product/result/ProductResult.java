package com.aionn.catalog.application.dto.product.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProductResult(
        String productId,
        String merchantId,
        String name,
        String brandId,
        List<String> categoryIds,
        List<String> imageList,
        List<String> tags,
        List<String> collectionIds,
        Map<String, String> attributes,
        List<VariantResult> variants,
        String aiDescription,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Double rating,
        Long reviewCount,
        Long soldCount,
        FlashSaleInfo flashSale,
        String provinceCode,
        String provinceName) {

    public record VariantResult(
            String skuId,
            Map<String, String> attributeValues,
            BigDecimal price,
            BigDecimal originalPrice,
            String currency) {
    }

    /**
     * Active flash-sale snapshot for the product. Null when no flash sale is
     * currently running. Carries the campaign end-time for the storefront
     * countdown and the per-SKU sale offers so the cart can resolve a sale
     * price for the picked variant.
     */
    public record FlashSaleInfo(
            String campaignId,
            Instant endAt,
            BigDecimal salePrice,
            String currency,
            Integer saleStock,
            Integer soldCount,
            List<SkuOffer> skuOffers) {

        public record SkuOffer(
                String skuId,
                BigDecimal salePrice,
                String currency,
                int saleStock,
                int soldCount) {
        }
    }
}
