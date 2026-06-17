package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.product.command.AssignBrandCommand;
import com.aionn.catalog.application.dto.product.command.CreateProductCommand;
import com.aionn.catalog.application.dto.product.command.PublishCommand;
import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.mapper.ProductResultMapper;
import com.aionn.catalog.application.port.out.AttributeTemplatePersistencePort;
import com.aionn.catalog.application.port.out.BrandPersistencePort;
import com.aionn.catalog.application.port.out.CategoryPersistencePort;
import com.aionn.catalog.application.port.out.MerchantPersistencePort;
import com.aionn.catalog.application.port.out.ProductPersistencePort;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import com.aionn.catalog.application.port.out.UserBrowsingHistoryPersistencePort;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.Brand;
import com.aionn.catalog.domain.model.Merchant;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.valueobject.BrandStatus;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.domain.vo.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductPersistencePort productRepository;
    @Mock
    private UserBrowsingHistoryPersistencePort userBrowsingHistoryRepository;
    @Mock
    private MerchantPersistencePort merchantRepository;
    @Mock
    private BrandPersistencePort brandRepository;
    @Mock
    private CategoryPersistencePort categoryRepository;
    @Mock
    private AttributeTemplatePersistencePort attributeTemplateRepository;
    @Mock
    private ProductResultMapper productResultMapper;
    @Mock
    private ProductSearchIndex searchIndex;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    private ProductResult sampleResult(String productId) {
        return new ProductResult(
                productId, "m-1", "Phone", null,
                List.of(), List.of(), List.of(), List.of(), Map.of(), List.of(),
                null, "DRAFT", Instant.now(), Instant.now(),
                0.0, 0L, 0L, null, null, null);
    }

    @Test
    void createResolvesMerchantAndPersistsProduct() {
        Merchant merchant = Merchant.register("m-1", "owner-1", "Acme");
        when(merchantRepository.findByOwnerId("owner-1")).thenReturn(Optional.of(merchant));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productResultMapper.toResult(any(Product.class)))
                .thenAnswer(inv -> sampleResult(((Product) inv.getArgument(0)).getProductId()));

        ProductResult result = productService.create(new CreateProductCommand("owner-1", "Phone"));

        assertThat(result.merchantId()).isEqualTo("m-1");
        assertThat(result.name()).isEqualTo("Phone");
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getMerchantId()).isEqualTo("m-1");
        verify(eventPublisher).publish(anyCollection());
    }

    @Test
    void createThrowsWhenOwnerHasNoMerchant() {
        when(merchantRepository.findByOwnerId("owner-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(new CreateProductCommand("owner-1", "Phone")))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.MERCHANT_NOT_FOUND.getCode());

        verify(productRepository, never()).save(any());
    }

    @Test
    void getReturnsResultWhenProductExists() {
        Product product = Product.create("p-1", "m-1", "Phone");
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(productResultMapper.toResult(product)).thenReturn(sampleResult("p-1"));

        ProductResult result = productService.get("p-1");

        assertThat(result.productId()).isEqualTo("p-1");
    }

    @Test
    void getThrowsWhenProductMissing() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.get("missing"))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.PRODUCT_NOT_FOUND.getCode());
    }

    @Test
    void assignBrandRejectsInactiveBrand() {
        Merchant merchant = Merchant.register("m-1", "owner-1", "Acme");
        Product product = Product.create("p-1", "m-1", "Phone");
        Brand brand = Brand.create("b-1", "Acme", null, null);
        brand.softDelete("policy");

        when(merchantRepository.findByOwnerId("owner-1")).thenReturn(Optional.of(merchant));
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(brandRepository.findById("b-1")).thenReturn(Optional.of(brand));

        assertThatThrownBy(() -> productService.assignBrand(new AssignBrandCommand("p-1", "owner-1", "b-1")))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.PRODUCT_BRAND_NOT_APPROVED.getCode());

        assertThat(brand.getStatus()).isNotEqualTo(BrandStatus.ACTIVE);
    }

    @Test
    void publishIndexesAndPublishesEvent() {
        Product product = Product.create("p-1", "m-1", "Phone");
        product.defineVariant("sku-1", Map.of("color", "red"), Money.of(new BigDecimal("100"), "VND"));
        product.categorize(List.of("cat-1"));
        product.pullEvents();

        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productResultMapper.toResult(product)).thenReturn(sampleResult("p-1"));

        productService.publish(new PublishCommand("p-1", "admin-1"));

        verify(searchIndex).index(any());
        verify(eventPublisher).publish(anyCollection());
    }
}
