package com.aionn.ucp.application.service;

import com.aionn.ucp.application.dto.checkout.CheckoutDtos;
import com.aionn.ucp.application.dto.envelope.UcpEnvelope;
import com.aionn.ucp.application.port.out.CartSessionPersistencePort;
import com.aionn.ucp.application.port.out.CartSessionPersistencePort.CartSession;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.LineCodec;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.LineItemSnapshot;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.Session;
import com.aionn.ucp.application.port.out.CatalogQueryPort;
import com.aionn.ucp.application.port.out.OrderPlacementPort;
import com.aionn.ucp.application.port.out.ShippingQueryPort;
import com.aionn.ucp.application.port.out.PromotionQueryPort;
import com.aionn.ucp.domain.model.CapabilityName;
import com.aionn.ucp.domain.model.CheckoutSessionStatus;
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
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UcpCheckoutServiceTest {

    @Mock
    private CheckoutSessionPersistencePort sessionRepository;

    @Mock
    private CartSessionPersistencePort cartRepository;

    @Mock
    private LineCodec lineCodec;

    @Mock
    private UcpEnvelopeFactory envelopeFactory;

    @Mock
    private UcpProperties properties;

    @Mock
    private CatalogQueryPort catalogQueryPort;

    @Mock
    private OrderPlacementPort orderPlacementPort;

    @Mock
    private ShippingQueryPort shippingQueryPort;

    @Mock
    private PromotionQueryPort promotionQueryPort;

    @InjectMocks
    private UcpCheckoutService checkoutService;

    private UcpEnvelope ucpEnvelope;

    @BeforeEach
    void setUp() {
        ucpEnvelope = UcpEnvelope.ok("2026-01-23", null);
        lenient().when(properties.getEndpointBaseUrl()).thenReturn("http://localhost:8080");
        lenient().when(envelopeFactory.ok(CapabilityName.CHECKOUT)).thenReturn(ucpEnvelope);
    }

    @Test
    @DisplayName("create() from cart_id should return existing checkout session if it already exists")
    void create_returnsExistingSession_whenCheckoutExistsForCart() {
        Session existingSession = new Session(
                "chk_1", "user-1", "http://platform/profile", "http://webhook",
                CheckoutSessionStatus.INCOMPLETE, "VND",
                "[{\"skuId\":\"sku_1\"}]", "{\"subtotal\":20000}",
                null,
                null, "cart_1", "http://localhost:8080/checkout?session=chk_1",
                Instant.now(), Instant.now());

        when(sessionRepository.findByCartId("cart_1")).thenReturn(Optional.of(existingSession));
        when(lineCodec.decode(anyString())).thenReturn(List.of(new LineItemSnapshot("sku_1", 2, 10000, "Variant Title")));

        CheckoutDtos.CreateRequest request = new CheckoutDtos.CreateRequest(
                "cart_1", null, null, null, null, null, "http://webhook");

        CheckoutDtos.CheckoutResponse response = checkoutService.create(request, "user-1");

        assertNotNull(response);
        assertEquals("chk_1", response.id());
        assertEquals("incomplete", response.status());
        verify(cartRepository, never()).findById(anyString());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("create() from cart_id should convert cart to new checkout session if none exists")
    void create_convertsCartToCheckout_whenNoSessionExists() {
        // Mock no existing session for cart
        when(sessionRepository.findByCartId("cart_1")).thenReturn(Optional.empty());

        // Mock cart load
        CartSession cart = new CartSession(
                "cart_1", "user-1", "VND",
                "[{\"skuId\":\"sku_1\",\"quantity\":2,\"unitPriceMinor\":10000,\"title\":\"Variant Title\"}]",
                "{\"subtotal\":20000,\"total\":20000}",
                "http://localhost:8080/cart?session=cart_1",
                Instant.now(), Instant.now());
        when(cartRepository.findById("cart_1")).thenReturn(Optional.of(cart));
        when(lineCodec.decode(cart.lineItemsJson())).thenReturn(List.of(
                new LineItemSnapshot("sku_1", 2, 10000, "Variant Title")
        ));

        CheckoutDtos.CreateRequest request = new CheckoutDtos.CreateRequest(
                "cart_1", null, null, null, null, null, "http://webhook");

        CheckoutDtos.CheckoutResponse response = checkoutService.create(request, "user-1");

        assertNotNull(response);
        assertTrue(response.id().startsWith("chk_"));
        assertEquals("incomplete", response.status());
        assertEquals("VND", response.currency());
        assertEquals(1, response.line_items().size());

        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    @DisplayName("complete() should pass stored voucher code to order placement")
    void complete_passesStoredVoucherCodeToOrderPlacement() {
        Session session = new Session(
                "chk_1", "user-1", null, "http://webhook",
                CheckoutSessionStatus.READY_FOR_COMPLETE, "VND",
                "[{\"skuId\":\"sku_1\",\"quantity\":2,\"unitPriceMinor\":10000,\"title\":\"Variant Title\"}]",
                "{\"subtotal\":20000,\"total\":15000}",
                "{\"voucherCode\":\"SAVE5\"}",
                null, null, "http://localhost:8080/checkout?session=chk_1",
                Instant.now(), Instant.now());

        when(sessionRepository.findById("chk_1")).thenReturn(Optional.of(session));
        when(lineCodec.decode(session.lineItemsJson())).thenReturn(List.of(
                new LineItemSnapshot("sku_1", 2, 10000, "Variant Title")));
        when(orderPlacementPort.place(any(OrderPlacementPort.PlaceCommand.class)))
                .thenReturn(new OrderPlacementPort.PlacedOrder("ord_1", 15000, "VND"));
        when(shippingQueryPort.getShippingOptions(any(), any(), anyLong(), any()))
                .thenReturn(List.of(new ShippingQueryPort.ShippingOption(
                        "standard", "Standard Shipping", "Arrives later", 0, "VND")));

        CheckoutDtos.CompleteRequest request = new CheckoutDtos.CompleteRequest("COD", "addr_1");

        checkoutService.complete("chk_1", "user-1", request);

        var captor = forClass(OrderPlacementPort.PlaceCommand.class);
        verify(orderPlacementPort).place(captor.capture());
        assertEquals("SAVE5", captor.getValue().voucherCode());
    }
}
