package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.product.command.BulkPriceUpdateCommand;
import com.aionn.catalog.application.dto.product.command.CreateProductCommand;
import com.aionn.catalog.application.dto.product.command.UpdateMediaCommand;
import com.aionn.catalog.application.mapper.ProductResultMapper;
import com.aionn.catalog.application.port.out.AttributeTemplatePersistencePort;
import com.aionn.catalog.application.port.out.BrandPersistencePort;
import com.aionn.catalog.application.port.out.CategoryPersistencePort;
import com.aionn.catalog.application.port.out.MerchantPersistencePort;
import com.aionn.catalog.application.port.out.ProductPersistencePort;
import com.aionn.catalog.application.port.out.UserBrowsingHistoryPersistencePort;
import com.aionn.catalog.domain.model.UserBrowsingHistory;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.Merchant;
import com.aionn.catalog.domain.model.Product;
import com.aionn.sharedkernel.application.port.EventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Regression tests for the merchant-id resolution wiring on owner-side
 * product operations. The pre-fix bug let any authenticated user act on
 * any merchant's products because the controllers passed
 * {@code authentication.getName()} (= userId) into the {@code merchantId}
 * slot of every command.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductPersistencePort productRepository;
    @Mock
    MerchantPersistencePort merchantRepository;
    @Mock
    BrandPersistencePort brandRepository;
    @Mock
    CategoryPersistencePort categoryRepository;
    @Mock
    AttributeTemplatePersistencePort attributeTemplateRepository;
    @Mock
    ProductResultMapper productResultMapper;
    @Mock
    ProductSearchIndex searchIndex;
    @Mock
    EventPublisher eventPublisher;
    @Mock
    UserBrowsingHistoryPersistencePort userBrowsingHistoryRepository;

    @InjectMocks
    ProductService productService;

    @Test
    @DisplayName("create() throws MERCHANT_NOT_FOUND when the authenticated user has no merchant")
    void create_throwsMerchantNotFound_whenOwnerHasNoMerchant() {
        when(merchantRepository.findByOwnerId("user-1")).thenReturn(Optional.empty());

        CatalogException ex = assertThrows(CatalogException.class,
                () -> productService.create(new CreateProductCommand("user-1", "Foo")));

        assertEquals(CatalogErrorCode.MERCHANT_NOT_FOUND.getCode(), ex.getErrorCode());
        verifyNoInteractions(productRepository, eventPublisher, searchIndex);
    }

    @Test
    @DisplayName("create() resolves merchantId from the authenticated owner instead of trusting the client")
    void create_resolvesMerchantIdFromOwner() {
        Merchant merchant = Merchant.register("M_1", "user-1", "Shop");
        when(merchantRepository.findByOwnerId("user-1")).thenReturn(Optional.of(merchant));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.create(new CreateProductCommand("user-1", "Foo"));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        // The product's merchantId is the resolved ULID, not the owner/user id.
        assertEquals("M_1", captor.getValue().getMerchantId());
    }

    @Test
    @DisplayName("updateMedia() rejects an attacker acting on another merchant's product")
    void updateMedia_rejectsForeignProduct() {
        Merchant attackerMerchant = Merchant.register("M_attacker", "attacker", "Attacker");
        when(merchantRepository.findByOwnerId("attacker")).thenReturn(Optional.of(attackerMerchant));
        Product victim = Product.create("P_1", "M_victim", "Victim Product");
        when(productRepository.findById("P_1")).thenReturn(Optional.of(victim));

        CatalogException ex = assertThrows(CatalogException.class,
                () -> productService.updateMedia(
                        new UpdateMediaCommand("P_1", "attacker", List.of("img.jpg"))));

        assertEquals(CatalogErrorCode.PRODUCT_FORBIDDEN.getCode(), ex.getErrorCode());
        verify(productRepository, never()).save(any());
        verifyNoInteractions(searchIndex);
    }

    @Test
    @DisplayName("updateMedia() throws MERCHANT_NOT_FOUND when the authenticated user has no merchant")
    void updateMedia_throwsMerchantNotFound_whenOwnerHasNoMerchant() {
        when(merchantRepository.findByOwnerId("user-1")).thenReturn(Optional.empty());

        CatalogException ex = assertThrows(CatalogException.class,
                () -> productService.updateMedia(
                        new UpdateMediaCommand("P_1", "user-1", List.of("img.jpg"))));

        assertEquals(CatalogErrorCode.MERCHANT_NOT_FOUND.getCode(), ex.getErrorCode());
        verifyNoInteractions(productRepository, searchIndex, eventPublisher);
    }

    @Test
    @DisplayName("bulkPriceUpdate() returns silently (no save, no events) when no SKUs match the merchant")
    void bulkPriceUpdate_returnsEarly_whenNothingMatches() {
        Merchant merchant = Merchant.register("M_1", "user-1", "Shop");
        when(merchantRepository.findByOwnerId("user-1")).thenReturn(Optional.of(merchant));
        when(productRepository.findByMerchantAndSkuIds("M_1", List.of("S_1"))).thenReturn(List.of());

        productService.bulkPriceUpdate(new BulkPriceUpdateCommand(
                "user-1", List.of("S_1"), BulkPriceUpdateCommand.ChangeType.SET,
                BigDecimal.TEN, "VND"));

        verify(productRepository, never()).save(any());
        verifyNoInteractions(eventPublisher, searchIndex);
    }

    @Test
    @DisplayName("getRelatedProducts() returns mapped products from port")
    void getRelatedProducts_returnsProducts() {
        Product product = Product.create("P_1", "M_1", "Base Product");
        Product related = Product.create("P_2", "M_1", "Related Product");
        when(productRepository.findById("P_1")).thenReturn(Optional.of(product));
        when(productRepository.findRelatedProducts("P_1", null, List.of(), 5))
                .thenReturn(List.of(related));

        productService.getRelatedProducts("P_1", 5);

        verify(productRepository).findRelatedProducts("P_1", null, List.of(), 5);
        verify(productResultMapper).toResult(related);
    }

    @Test
    @DisplayName("getPopularProducts() returns mapped popular products from port")
    void getPopularProducts_returnsProducts() {
        Product popular = Product.create("P_1", "M_1", "Popular Product");
        when(productRepository.findPopularProducts(5)).thenReturn(List.of(popular));

        productService.getPopularProducts(5);

        verify(productRepository).findPopularProducts(5);
        verify(productResultMapper).toResult(popular);
    }

    @Test
    @DisplayName("getPersonalizedProducts() returns mapped personalized products from database when user is logged in")
    void getPersonalizedProducts_returnsProductsFromDb_whenUserLoggedIn() {
        Product personalized = Product.create("P_1", "M_1", "Personalized Product");
        UserBrowsingHistory history = new UserBrowsingHistory("user-1", List.of("C_1"), List.of("B_1"));
        when(userBrowsingHistoryRepository.findByUserId("user-1")).thenReturn(Optional.of(history));
        when(productRepository.findPersonalizedProducts(List.of("C_1"), List.of("B_1"), 5))
                .thenReturn(List.of(personalized));

        productService.getPersonalizedProducts("user-1", List.of(), List.of(), 5);

        verify(userBrowsingHistoryRepository).findByUserId("user-1");
        verify(productRepository).findPersonalizedProducts(List.of("C_1"), List.of("B_1"), 5);
        verify(productResultMapper).toResult(personalized);
    }

    @Test
    @DisplayName("getPersonalizedProducts() returns mapped personalized products from query parameters when user is anonymous")
    void getPersonalizedProducts_returnsProductsFromQuery_whenUserAnonymous() {
        Product personalized = Product.create("P_1", "M_1", "Personalized Product");
        when(productRepository.findPersonalizedProducts(List.of("C_1"), List.of("B_1"), 5))
                .thenReturn(List.of(personalized));

        productService.getPersonalizedProducts(null, List.of("C_1"), List.of("B_1"), 5);

        verify(productRepository).findPersonalizedProducts(List.of("C_1"), List.of("B_1"), 5);
        verify(userBrowsingHistoryRepository, never()).findByUserId(any());
        verify(productResultMapper).toResult(personalized);
    }

    @Test
    @DisplayName("getPersonalizedProducts() falls back to popular products when preferences are empty")
    void getPersonalizedProducts_fallsBackToPopular() {
        Product popular = Product.create("P_1", "M_1", "Popular Product");
        when(productRepository.findPopularProducts(5)).thenReturn(List.of(popular));

        productService.getPersonalizedProducts(null, List.of(), List.of(), 5);

        verify(productRepository).findPopularProducts(5);
        verify(productRepository, never()).findPersonalizedProducts(any(), any(), any(Integer.class));
    }

    @Test
    @DisplayName("trackProductView() saves history for logged in user")
    void trackProductView_savesHistory_whenUserLoggedIn() {
        Product product = Product.create("P_1", "M_1", "Sample Product");
        product.categorize(List.of("C_1"));
        product.assignBrand("B_1");
        when(productRepository.findById("P_1")).thenReturn(Optional.of(product));
        when(userBrowsingHistoryRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        productService.trackProductView("P_1", "user-1");

        ArgumentCaptor<UserBrowsingHistory> captor = ArgumentCaptor.forClass(UserBrowsingHistory.class);
        verify(userBrowsingHistoryRepository).save(captor.capture());
        assertEquals("user-1", captor.getValue().getUserId());
        assertEquals(List.of("C_1"), captor.getValue().getCategoryIds());
        assertEquals(List.of("B_1"), captor.getValue().getBrandIds());
    }
}
