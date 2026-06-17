package com.aionn.catalog.application.mapper;

import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.domain.model.Product;
import com.aionn.sharedkernel.domain.vo.Money;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductResultMapperTest {

    private static final String PRODUCT_ID = "01HZPRD0000000000000000001";
    private static final String MERCHANT_ID = "01HZMRC0000000000000000001";

    private final ProductResultMapper mapper = Mappers.getMapper(ProductResultMapper.class);

    @Test
    void toResultMapsAllFields() {
        Product product = Product.create(PRODUCT_ID, MERCHANT_ID, "Acme");
        product.assignBrand("01HZBRD0000000000000000001");
        product.categorize(List.of("01HZCAT0000000000000000001"));
        product.defineVariant("01HZSKU0000000000000000001",
                Map.of("size", "M"), Money.of(new BigDecimal("100"), "VND"));

        ProductResult result = mapper.toResult(product);

        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.merchantId()).isEqualTo(MERCHANT_ID);
        assertThat(result.variants()).hasSize(1);
        assertThat(result.variants().get(0).price()).isEqualByComparingTo("100");
        assertThat(result.variants().get(0).currency()).isEqualTo("VND");
        assertThat(result.status()).isEqualTo("DRAFT");
    }

    @Test
    void toSearchDocumentComputesPriceRangeAndCurrency() {
        Product product = Product.create(PRODUCT_ID, MERCHANT_ID, "Acme");
        product.defineVariant("sku-1", Map.of("size", "M"), Money.of(new BigDecimal("100"), "VND"));
        product.defineVariant("sku-2", Map.of("size", "L"), Money.of(new BigDecimal("250"), "VND"));

        ProductSearchDocument doc = mapper.toSearchDocument(product, Map.of("size", "M"));

        assertThat(doc.priceFrom()).isEqualByComparingTo("100");
        assertThat(doc.priceTo()).isEqualByComparingTo("250");
        assertThat(doc.currency()).isEqualTo("VND");
        assertThat(doc.filterableAttributes()).containsEntry("size", "M");
    }

    @Test
    void toSearchDocumentHandlesProductWithoutPricedVariants() {
        Product product = Product.create(PRODUCT_ID, MERCHANT_ID, "Acme");

        ProductSearchDocument doc = mapper.toSearchDocument(product, Map.of());

        assertThat(doc.priceFrom()).isNull();
        assertThat(doc.priceTo()).isNull();
        assertThat(doc.currency()).isNull();
    }

    @Test
    void toResultPreservesNullPriceVariants() {
        Product product = Product.create(PRODUCT_ID, MERCHANT_ID, "Acme");
        product.defineVariant("sku-1", Map.of("size", "M"), null);

        ProductResult result = mapper.toResult(product);

        assertThat(result.variants()).hasSize(1);
        assertThat(result.variants().get(0).price()).isNull();
        assertThat(result.variants().get(0).currency()).isNull();
    }
}
