package com.aionn.payment.adapter.rest.controller;

import com.aionn.payment.adapter.rest.exception.PaymentExceptionHandler;
import com.aionn.payment.adapter.rest.support.session.CurrentUserIdArgumentResolver;
import com.aionn.payment.application.dto.payment.command.InitiatePaymentCommand;
import com.aionn.payment.application.dto.payment.command.RefundPaymentCommand;
import com.aionn.payment.application.dto.payment.result.PaymentResult;
import com.aionn.payment.application.service.PaymentService;
import com.aionn.payment.domain.exception.PaymentErrorCode;
import com.aionn.payment.domain.exception.PaymentException;
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
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerWebTest {

    @Mock
    private PaymentService paymentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentController controller = new PaymentController(paymentService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new PaymentExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        Jackson2ObjectMapperBuilder.json().build()))
                .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user-123", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static PaymentResult sampleResult(String paymentId, String status) {
        Instant now = Instant.now();
        return new PaymentResult(
                paymentId, "order-1", "user-123", null,
                new BigDecimal("100.00"), BigDecimal.ZERO, "VND",
                "STRIPE", status, "txn-1", null, null, null,
                now, now, now, null);
    }

    @Test
    void initiateCreatesPayment() throws Exception {
        when(paymentService.initiate(any(InitiatePaymentCommand.class)))
                .thenReturn(sampleResult("pay-1", "INITIATED"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "order-1",
                                  "amount": 100.00,
                                  "currency": "VND",
                                  "gateway": "STRIPE",
                                  "idempotencyKey": "idem-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paymentId").value("pay-1"))
                .andExpect(jsonPath("$.data.status").value("INITIATED"));

        verify(paymentService).initiate(any(InitiatePaymentCommand.class));
    }

    @Test
    void initiateRejectsBlankOrderId() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "",
                                  "amount": 100.00,
                                  "currency": "VND",
                                  "gateway": "STRIPE",
                                  "idempotencyKey": "idem-1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void initiateMissingMethodReturnsNotFound() throws Exception {
        when(paymentService.initiate(any(InitiatePaymentCommand.class)))
                .thenThrow(new PaymentException(PaymentErrorCode.METHOD_NOT_FOUND));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "order-1",
                                  "paymentMethodId": "missing",
                                  "amount": 50.00,
                                  "currency": "VND",
                                  "gateway": "STRIPE",
                                  "idempotencyKey": "idem-9"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value(PaymentErrorCode.METHOD_NOT_FOUND.getCode()));
    }

    @Test
    void refundReturnsRefundedPayment() throws Exception {
        when(paymentService.refund(any(RefundPaymentCommand.class)))
                .thenReturn(sampleResult("pay-2", "REFUNDED"));

        mockMvc.perform(post("/api/v1/payments/pay-2/refund")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100.00,
                                  "currency": "VND",
                                  "reason": "duplicate"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentId").value("pay-2"))
                .andExpect(jsonPath("$.data.status").value("REFUNDED"));

        verify(paymentService).refund(any(RefundPaymentCommand.class));
    }

    @Test
    void getReturnsPaymentForCurrentUser() throws Exception {
        when(paymentService.getForUser("pay-3", "user-123"))
                .thenReturn(sampleResult("pay-3", "PAID"));

        mockMvc.perform(get("/api/v1/payments/pay-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentId").value("pay-3"))
                .andExpect(jsonPath("$.data.status").value("PAID"));

        verify(paymentService).getForUser("pay-3", "user-123");
    }

    @Test
    void listByOrderReturnsPayments() throws Exception {
        when(paymentService.listByOrderId("order-9"))
                .thenReturn(List.of(sampleResult("pay-4", "PAID")));

        mockMvc.perform(get("/api/v1/payments/by-order/order-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].paymentId").value("pay-4"));

        verify(paymentService).listByOrderId("order-9");
    }
}
