package com.aionn.catalog.domain.model;

import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.sharedkernel.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static final String PRODUCT_ID = "01HZPRD0000000000000000001";
    private static final String MERCHANT_ID = "01HZMER0000000000000000001";

    private Money vnd(String amount) {
        return Money.of(new BigDecimal(amount), "VND");
    }

    @Test
    void createInitializesAsDraftAndEmitsEvent() {
        Product product = Product.create(PRODUCT_ID, MERCHANT_ID, "Phone");

        assertThat(product.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(product.getMerchantId()).isEqualTo(MERCHANT_ID);
        assertThat(product.getName()).isEqualTo("Phone");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.pullEvents()).hasSize(1);
    }

    @Test
    void createRejectsBlankName() {
        assertThatThrownBy(() -> Product.create(PRODUCT_ID, MERCHANT_ID, " "))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.INVALID_ARGUMENT.getCode());
    }

    @Test
    void defineVariantAddsToVariantList() {
        Product product = Product.create(PRODUCT_ID, MERCHANT_ID, "Phone");
        product.pullEvents();

        product.defineVariant("SKU-1", Map.of("color", "red"), vnd("100"));

        assertThat(product.variants()).hasSize(1);
        assertThat(product.findVariant("SKU-1")).isPresent();
        assertThat(product.pullEvents()).hasSize(1);
    }

    @Test
    void defineVariantRejectsDuplicateSku() {
        Product product = Product.create(PRODUCT_ID, MERCHANT_ID, "Phone");
        product.defineVariant("SKU-1", Map.of("color", "red"), vnd("100"));

        assertThatThrownBy(() -> product.defineVariant("SKU-1", Map.of("color", "blue"), vnd("100")))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.PRODUCT_VARIANT_DUPLICATE.getCode());
    }

    @Test
    void publishRequiresVariantAndCategory() {
        Product product = Product.create(PRODUCT_ID, MERCHANT_ID, "Phone");

        assertThatThrownBy(() -> product.publish("admin-1"))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.PRODUCT_PUBLISH_REQUIREMENTS.getCode());
    }

    @Test
    void ensureOwnedByThrowsForOtherMerchant() {
        Product product = Product.create(PRODUCT_ID, MERCHANT_ID, "Phone");

        assertThatThrownBy(() -> product.ensureOwnedBy("other-merchant"))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.PRODUCT_FORBIDDEN.getCode());
    }

    @Test
    void categorizeReplacesAndEmitsEvent() {
        Product product = Product.create(PRODUCT_ID, MERCHANT_ID, "Phone");
        product.pullEvents();

        product.categorize(List.of("cat-1", "cat-2"));

        assertThat(product.categoryIds()).containsExactly("cat-1", "cat-2");
        assertThat(product.pullEvents()).hasSize(1);
    }
}
