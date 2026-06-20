package com.aionn.ordering.adapter.rest.controller;

import com.aionn.ordering.adapter.rest.exception.OrderingExceptionHandler;
import com.aionn.ordering.adapter.rest.support.session.CurrentUserIdArgumentResolver;
import com.aionn.ordering.application.dto.order.command.CancelOrderCommand;
import com.aionn.ordering.application.dto.order.command.PlaceOrderCommand;
import com.aionn.ordering.application.dto.order.result.OrderResult;
import com.aionn.ordering.application.service.OrderService;
import com.aionn.ordering.domain.exception.OrderingErrorCode;
import com.aionn.ordering.domain.exception.OrderingException;
import com.aionn.sharedkernel.adapter.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerWebTest {

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new OrderingExceptionHandler(), new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        Jackson2ObjectMapperBuilder.json().build()))
                .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver())
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user-1", "n/a", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static OrderResult sampleResult(String status) {
        return new OrderResult("order-1", null, "user-1", "merchant-1", "prop-1",
                "COD", null, "VND", BigDecimal.valueOf(200),
                BigDecimal.ZERO, "addr-1", List.of(), status, null,
                Instant.now(), Instant.now(), null, null);
    }

    @Test
    void placeOrderReturnsCreated() throws Exception {
        when(orderService.placeOrder(any(PlaceOrderCommand.class)))
                .thenReturn(sampleResult("APPROVED"));

        mockMvc.perform(post("/api/v1/ordering/orders")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "addressId": "addr-1",
                                  "paymentMethodId": "COD",
                                  "currency": "VND",
                                  "shippingFee": 0,
                                  "gateway": "STRIPE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderId").value("order-1"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        verify(orderService).placeOrder(any(PlaceOrderCommand.class));
    }

    @Test
    void placeOrderRejectsBlankAddressId() throws Exception {
        mockMvc.perform(post("/api/v1/ordering/orders")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "addressId": "",
                                  "paymentMethodId": "COD",
                                  "currency": "VND",
                                  "shippingFee": 0,
                                  "gateway": "STRIPE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));

        verifyNoInteractions(orderService);
    }

    @Test
    void cancelOrderReturnsCancelledOrder() throws Exception {
        when(orderService.cancel(any(CancelOrderCommand.class)))
                .thenReturn(sampleResult("CANCELLED"));

        mockMvc.perform(post("/api/v1/ordering/orders/order-1/cancel")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "changed mind"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        verify(orderService).cancel(any(CancelOrderCommand.class));
    }

    @Test
    void cancelOrderRejectsBlankReason() throws Exception {
        mockMvc.perform(post("/api/v1/ordering/orders/order-1/cancel")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));

        verifyNoInteractions(orderService);
    }

    @Test
    void getOrderReturns404WhenOrderUnknown() throws Exception {
        when(orderService.getForRequester("missing", "user-1"))
                .thenThrow(new OrderingException(OrderingErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/ordering/orders/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderReturnsOk() throws Exception {
        when(orderService.getForRequester("order-1", "user-1"))
                .thenReturn(sampleResult("PENDING"));

        mockMvc.perform(get("/api/v1/ordering/orders/order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value("order-1"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void listMineReturnsOrders() throws Exception {
        when(orderService.listByUser("user-1", 20))
                .thenReturn(List.of(sampleResult("PENDING")));

        mockMvc.perform(get("/api/v1/ordering/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].orderId").value("order-1"));
    }

    @Test
    void listMineWithoutAuthReturnsForbidden() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/ordering/orders"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value("ORD_102"));

        verifyNoInteractions(orderService);
    }
}
