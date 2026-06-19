package com.aionn.ordering.adapter.rest.controller;

import com.aionn.ordering.adapter.rest.exception.OrderingExceptionHandler;
import com.aionn.ordering.adapter.rest.support.session.CurrentUserIdArgumentResolver;
import com.aionn.ordering.application.dto.returns.command.ApproveReturnCommand;
import com.aionn.ordering.application.dto.returns.command.RejectReturnCommand;
import com.aionn.ordering.application.dto.returns.command.RequestReturnCommand;
import com.aionn.ordering.application.dto.returns.result.ReturnResult;
import com.aionn.ordering.application.service.OrderReturnService;
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
class OrderReturnControllerWebTest {

    @Mock
    private OrderReturnService returnService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OrderReturnController controller = new OrderReturnController(returnService);
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

    private static ReturnResult sampleResult(String status) {
        return new ReturnResult("return-1", "order-1", "user-1", "merchant-1",
                "broken", null, null, null, null, null, null, status,
                Instant.now(), null, null);
    }

    @Test
    void requestReturnReturnsCreated() throws Exception {
        when(returnService.requestReturn(any(RequestReturnCommand.class)))
                .thenReturn(sampleResult("REQUESTED"));

        mockMvc.perform(post("/api/v1/ordering/returns/orders/order-1")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "broken on arrival",
                                  "evidenceUrl": "https://img.example.com/x.jpg"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.returnId").value("return-1"))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));

        verify(returnService).requestReturn(any(RequestReturnCommand.class));
    }

    @Test
    void requestReturnRejectsBlankReason() throws Exception {
        mockMvc.perform(post("/api/v1/ordering/returns/orders/order-1")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "",
                                  "evidenceUrl": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));

        verifyNoInteractions(returnService);
    }

    @Test
    void approveReturnReturnsApprovedStatus() throws Exception {
        when(returnService.approve(any(ApproveReturnCommand.class)))
                .thenReturn(sampleResult("APPROVED"));

        mockMvc.perform(post("/api/v1/ordering/returns/return-1/approve")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "refundAmount": 100,
                                  "currency": "VND",
                                  "returnWarehouseId": "wh-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        verify(returnService).approve(any(ApproveReturnCommand.class));
    }

    @Test
    void rejectReturnReturnsRejectedStatus() throws Exception {
        when(returnService.reject(any(RejectReturnCommand.class)))
                .thenReturn(sampleResult("REJECTED"));

        mockMvc.perform(post("/api/v1/ordering/returns/return-1/reject")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "no proof"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void getReturnReturns404WhenUnknown() throws Exception {
        when(returnService.getForRequester("missing", "user-1"))
                .thenThrow(new OrderingException(OrderingErrorCode.RETURN_NOT_FOUND));

        mockMvc.perform(get("/api/v1/ordering/returns/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listMineReturnsResults() throws Exception {
        when(returnService.listMine("user-1", 50))
                .thenReturn(List.of(sampleResult("REQUESTED")));

        mockMvc.perform(get("/api/v1/ordering/returns/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].returnId").value("return-1"));
    }
}
