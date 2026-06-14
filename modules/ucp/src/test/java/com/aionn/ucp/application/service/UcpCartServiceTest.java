package com.aionn.ucp.application.service;

import com.aionn.ucp.application.dto.cart.CartDtos;
import com.aionn.ucp.application.dto.catalog.CatalogProductDto;
import com.aionn.ucp.application.dto.checkout.CheckoutDtos;
import com.aionn.ucp.application.dto.envelope.UcpEnvelope;
import com.aionn.ucp.application.port.out.CartSessionPersistencePort;
import com.aionn.ucp.application.port.out.CartSessionPersistencePort.CartSession;
import com.aionn.ucp.application.port.out.CatalogQueryPort;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.LineCodec;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.LineItemSnapshot;
import com.aionn.ucp.domain.exception.UcpErrorCode;
import com.aionn.ucp.domain.exception.UcpException;
import com.aionn.ucp.domain.model.CapabilityName;
import com.aionn.ucp.infrastructure.config.UcpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UcpCartServiceTest {

    @Mock
    private CatalogQueryPort catalogQueryPort;

    @Mock
    private CartSessionPersistencePort cartRepository;

    @Mock
    private LineCodec lineCodec;

    @Mock
    private UcpEnvelopeFactory envelopeFactory;

    @Mock
    private UcpProperties properties;

    @InjectMocks
    private UcpCartService cartService;

    private UcpEnvelope ucpEnvelope;

    @BeforeEach
    void setUp() {
        ucpEnvelope = UcpEnvelope.ok("2026-01-23", null);
        lenient().when(properties.getEndpointBaseUrl()).thenReturn("http://localhost:8080");
        lenient().when(envelopeFactory.ok(CapabilityName.CART)).thenReturn(ucpEnvelope);
    }

    @Test
    @DisplayName("create() should build cart snapshot and persist to repository")
    void create_savesCartSession_whenValidRequest() {
        // Prepare request
        CheckoutDtos.LineItemRef itemRef = new CheckoutDtos.LineItemRef("sku_1");
        CheckoutDtos.CreateLineItem lineItem = new CheckoutDtos.CreateLineItem(itemRef, 2);
        CartDtos.CreateRequest request = new CartDtos.CreateRequest(List.of(lineItem), null, null);

        // Mock catalog lookup
        CatalogProductDto.Variant variant = new CatalogProductDto.Variant(
                "sku_1", "sku_1", "Variant Title", null, new com.aionn.ucp.application.dto.envelope.Price(10000L, "VND"),
                new CatalogProductDto.Availability(true), List.of(), List.of());
        CatalogProductDto product = new CatalogProductDto(
                "p_1", "product-handle", "Product Title", null, null, List.of(),
                null, List.of(), List.of(), List.of(), List.of(variant), null);
        when(catalogQueryPort.lookup(List.of("sku_1")))
                .thenReturn(new CatalogQueryPort.LookupResult(List.of(product), List.of()));

        // Mock snapshot encoding
        when(lineCodec.encode(anyList())).thenReturn("[{\"skuId\":\"sku_1\"}]");

        // Act
        CartDtos.CartResponse response = cartService.create(request, "user-1");

        // Assert
        assertNotNull(response);
        assertEquals(ucpEnvelope, response.ucp());
        assertEquals("VND", response.currency());
        assertEquals(1, response.line_items().size());
        assertEquals(20000L, response.line_items().get(0).totals().get(0).amount());

        verify(cartRepository).save(any(CartSession.class));
    }

    @Test
    @DisplayName("get() should retrieve and reconstruct cart response")
    void get_returnsCartResponse_whenSessionExists() {
        CartSession session = new CartSession(
                "cart_1", "user-1", "VND",
                "[{\"skuId\":\"sku_1\",\"quantity\":2,\"unitPriceMinor\":10000,\"title\":\"Variant Title\"}]",
                "{\"subtotal\":20000,\"total\":20000}",
                "http://localhost:8080/cart?session=cart_1",
                Instant.now(), Instant.now());

        when(cartRepository.findById("cart_1")).thenReturn(Optional.of(session));
        when(lineCodec.decode(session.lineItemsJson())).thenReturn(List.of(
                new LineItemSnapshot("sku_1", 2, 10000, "Variant Title")
        ));

        CartDtos.CartResponse response = cartService.get("cart_1");

        assertNotNull(response);
        assertEquals("cart_1", response.id());
        assertEquals("VND", response.currency());
        assertEquals(1, response.line_items().size());
        assertEquals("Variant Title", response.line_items().get(0).item().title());
    }

    @Test
    @DisplayName("get() should throw CART_NOT_FOUND when session is missing")
    void get_throwsCartNotFound_whenSessionDoesNotExist() {
        when(cartRepository.findById("cart_invalid")).thenReturn(Optional.empty());

        UcpException ex = assertThrows(UcpException.class, () -> cartService.get("cart_invalid"));
        assertEquals(UcpErrorCode.CART_NOT_FOUND.getCode(), ex.getErrorCode());
    }

    @Test
    @DisplayName("update() should modify cart session and save changes")
    void update_savesUpdatedCartSession_whenOwnerMatches() {
        CartSession session = new CartSession(
                "cart_1", "user-1", "VND",
                "[{\"skuId\":\"sku_1\",\"quantity\":2,\"unitPriceMinor\":10000,\"title\":\"Variant Title\"}]",
                "{\"subtotal\":20000,\"total\":20000}",
                "http://localhost:8080/cart?session=cart_1",
                Instant.now(), Instant.now());

        CheckoutDtos.LineItemRef itemRef = new CheckoutDtos.LineItemRef("sku_1");
        CheckoutDtos.CreateLineItem updatedItem = new CheckoutDtos.CreateLineItem(itemRef, 5);
        CartDtos.UpdateRequest request = new CartDtos.UpdateRequest("cart_1", List.of(updatedItem), null, null);

        CatalogProductDto.Variant variant = new CatalogProductDto.Variant(
                "sku_1", "sku_1", "Variant Title", null, new com.aionn.ucp.application.dto.envelope.Price(10000L, "VND"),
                new CatalogProductDto.Availability(true), List.of(), List.of());
        CatalogProductDto product = new CatalogProductDto(
                "p_1", "product-handle", "Product Title", null, null, List.of(),
                null, List.of(), List.of(), List.of(), List.of(variant), null);

        when(cartRepository.findById("cart_1")).thenReturn(Optional.of(session));
        when(catalogQueryPort.lookup(List.of("sku_1")))
                .thenReturn(new CatalogQueryPort.LookupResult(List.of(product), List.of()));
        when(lineCodec.encode(anyList())).thenReturn("[{\"skuId\":\"sku_1\",\"quantity\":5}]");

        CartDtos.CartResponse response = cartService.update("cart_1", "user-1", request);

        assertNotNull(response);
        assertEquals(5, response.line_items().get(0).quantity());
        verify(cartRepository).save(any(CartSession.class));
    }

    @Test
    @DisplayName("cancel() should delete cart session from database")
    void cancel_deletesSession_whenOwnerMatches() {
        CartSession session = new CartSession(
                "cart_1", "user-1", "VND",
                "[{\"skuId\":\"sku_1\",\"quantity\":2,\"unitPriceMinor\":10000,\"title\":\"Variant Title\"}]",
                "{\"subtotal\":20000,\"total\":20000}",
                "http://localhost:8080/cart?session=cart_1",
                Instant.now(), Instant.now());

        when(cartRepository.findById("cart_1")).thenReturn(Optional.of(session));
        when(lineCodec.decode(session.lineItemsJson())).thenReturn(List.of(
                new LineItemSnapshot("sku_1", 2, 10000, "Variant Title")
        ));

        CartDtos.CartResponse response = cartService.cancel("cart_1", "user-1");

        assertNotNull(response);
        verify(cartRepository).deleteById("cart_1");
    }
}
