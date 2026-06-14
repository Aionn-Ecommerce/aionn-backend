package com.aionn.ordering.application.service;

import com.aionn.ordering.application.dto.order.command.ConfirmPreparationCommand;
import com.aionn.ordering.application.mapper.OrderingResultMapper;
import com.aionn.ordering.application.port.out.CartPersistencePort;
import com.aionn.ordering.application.port.out.CatalogPricingGateway;
import com.aionn.ordering.application.port.out.OrderPersistencePort;
import com.aionn.ordering.application.port.out.PaymentGateway;
import com.aionn.ordering.application.port.out.ShippingGateway;
import com.aionn.ordering.application.port.out.StockReservationGateway;
import com.aionn.ordering.application.port.out.VoucherGateway;
import com.aionn.ordering.application.port.out.integration.OrderingIntegrationEventPublisherPort;
import com.aionn.ordering.domain.exception.OrderingErrorCode;
import com.aionn.ordering.domain.exception.OrderingException;
import com.aionn.ordering.domain.model.Order;
import com.aionn.ordering.domain.model.OrderItem;
import com.aionn.ordering.domain.valueobject.OrderStatus;
import com.aionn.ordering.infrastructure.config.OrderingProperties;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.sharedkernel.integration.port.catalog.MerchantQueryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the B3 ownership wiring (resolve merchantId from authenticated
 * owner) and H3 cross-user data leak fix on the get endpoint.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    CartPersistencePort cartRepository;
    @Mock
    OrderPersistencePort orderRepository;
    @Mock
    OrderingResultMapper mapper;
    @Mock
    EventPublisher eventPublisher;
    @Mock
    StockReservationGateway stockReservationGateway;
    @Mock
    PaymentGateway paymentGateway;
    @Mock
    ShippingGateway shippingGateway;
    @Mock
    CatalogPricingGateway catalogPricingGateway;
    @Mock
    VoucherGateway voucherGateway;
    @Mock
    CartService cartService;
    @Mock
    MerchantQueryPort merchantQueryPort;
    @Mock
    OrderingIntegrationEventPublisherPort integrationEventPublisher;
    @Mock
    OrderingProperties properties;

    @InjectMocks
    OrderService orderService;

    @Test
    @DisplayName("confirmPreparation() throws when the authenticated user has no merchant")
    void confirmPreparation_throwsWhenOwnerHasNoMerchant() {
        when(merchantQueryPort.findMerchantIdByOwnerId("user-1")).thenReturn(Optional.empty());

        OrderingException ex = assertThrows(OrderingException.class,
                () -> orderService.confirmPreparation(new ConfirmPreparationCommand("O_1", "user-1")));

        assertEquals(OrderingErrorCode.ORDER_NOT_OWNED_BY_MERCHANT.getCode(), ex.getErrorCode());
        verify(orderRepository, never()).findById(any());
    }

    @Test
    @DisplayName("confirmPreparation() rejects when the resolved merchant does not own the order")
    void confirmPreparation_rejectsForeignOrder() {
        when(merchantQueryPort.findMerchantIdByOwnerId("attacker")).thenReturn(Optional.of("M_attacker"));
        Order victim = approvedOrder("O_1", "U_victim", "M_victim");
        when(orderRepository.findById("O_1")).thenReturn(Optional.of(victim));

        OrderingException ex = assertThrows(OrderingException.class,
                () -> orderService.confirmPreparation(new ConfirmPreparationCommand("O_1", "attacker")));

        assertEquals(OrderingErrorCode.ORDER_NOT_OWNED_BY_MERCHANT.getCode(), ex.getErrorCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("getForRequester() rejects unrelated callers")
    void getForRequester_rejectsUnrelatedCaller() {
        Order order = approvedOrder("O_1", "U_buyer", "M_seller");
        when(orderRepository.findById("O_1")).thenReturn(Optional.of(order));
        when(merchantQueryPort.findMerchantIdByOwnerId("nosey")).thenReturn(Optional.empty());

        OrderingException ex = assertThrows(OrderingException.class,
                () -> orderService.getForRequester("O_1", "nosey"));

        assertEquals(OrderingErrorCode.ORDER_FORBIDDEN.getCode(), ex.getErrorCode());
    }

    @Test
    @DisplayName("getForRequester() allows the buyer of the order")
    void getForRequester_allowsBuyer() {
        Order order = approvedOrder("O_1", "U_buyer", "M_seller");
        when(orderRepository.findById("O_1")).thenReturn(Optional.of(order));

        orderService.getForRequester("O_1", "U_buyer");

        verify(mapper).toResult(order);
    }

    @Test
    @DisplayName("getForRequester() allows the merchant owner of the order")
    void getForRequester_allowsMerchant() {
        Order order = approvedOrder("O_1", "U_buyer", "M_seller");
        when(orderRepository.findById("O_1")).thenReturn(Optional.of(order));
        when(merchantQueryPort.findMerchantIdByOwnerId("seller-user")).thenReturn(Optional.of("M_seller"));

        orderService.getForRequester("O_1", "seller-user");

        verify(mapper).toResult(order);
    }

    private static Order approvedOrder(String orderId, String userId, String merchantId) {
        OrderItem item = new OrderItem("SKU_1", 1, Money.of(new BigDecimal("99000"), "VND"),
                "W_1", "RES_1");
        Order order = new Order(orderId, null, userId, merchantId, "prop", "pm",
                "VND", List.of(item), null, Money.zero("VND"), Money.of(new BigDecimal("99000"), "VND"),
                OrderStatus.APPROVED, "PAY_1", null, Instant.now(), Instant.now(), null, null);
        order.pullEvents();
        return order;
    }
}
