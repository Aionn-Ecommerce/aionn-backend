package com.aionn.ordering.adapter.rest.controller;

import com.aionn.ordering.adapter.rest.exception.OrderingExceptionHandler;
import com.aionn.ordering.adapter.rest.support.session.CurrentUserIdArgumentResolver;
import com.aionn.ordering.application.dto.cart.command.AddItemCommand;
import com.aionn.ordering.application.dto.cart.command.ApplyVoucherCommand;
import com.aionn.ordering.application.dto.cart.command.ClearCartCommand;
import com.aionn.ordering.application.dto.cart.command.RemoveItemCommand;
import com.aionn.ordering.application.dto.cart.command.RemoveVoucherCommand;
import com.aionn.ordering.application.dto.cart.command.UpdateItemQtyCommand;
import com.aionn.ordering.application.dto.cart.result.CartResult;
import com.aionn.ordering.application.service.CartService;
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
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerWebTest {

    @Mock
    private CartService cartService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CartController controller = new CartController(cartService);
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

    private static CartResult sampleCart(int itemCount) {
        Instant now = Instant.now();
        List<CartResult.CartItemResult> items = itemCount == 0
                ? List.of()
                : List.of(new CartResult.CartItemResult("sku-1", 2));
        return new CartResult("cart-1", "user-1", items, null, now, now);
    }

    // ---------- get my cart ----------

    @Test
    void getMyCartReturnsCart() throws Exception {
        when(cartService.getMyCart("user-1")).thenReturn(sampleCart(1));

        mockMvc.perform(get("/api/v1/ordering/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartId").value("cart-1"))
                .andExpect(jsonPath("$.data.userId").value("user-1"));
    }

    @Test
    void getMyCartWithoutAuthReturns403() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/ordering/cart"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value("ORD_102"));

        verifyNoInteractions(cartService);
    }

    // ---------- add item ----------

    @Test
    void addItemReturns200() throws Exception {
        when(cartService.addItem(any(AddItemCommand.class))).thenReturn(sampleCart(1));

        mockMvc.perform(post("/api/v1/ordering/cart/items")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "skuId": "sku-1",
                          "qty": 2
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartId").value("cart-1"));

        verify(cartService).addItem(any());
    }

    @Test
    void addItemRejectsBlankSkuId() throws Exception {
        mockMvc.perform(post("/api/v1/ordering/cart/items")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "skuId": "",
                          "qty": 1
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));

        verifyNoInteractions(cartService);
    }

    @Test
    void addItemRejectsZeroQty() throws Exception {
        mockMvc.perform(post("/api/v1/ordering/cart/items")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "skuId": "sku-1",
                          "qty": 0
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void addItemWhenServiceRejectsQtyTooLargeReturns400() throws Exception {
        when(cartService.addItem(any(AddItemCommand.class)))
                .thenThrow(new OrderingException(OrderingErrorCode.INVALID_ARGUMENT,
                        "Quantity per item cannot exceed 999"));

        mockMvc.perform(post("/api/v1/ordering/cart/items")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "skuId": "sku-1",
                          "qty": 1000
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("ORD_900"));
    }

    @Test
    void addItemWhenForbiddenReturns403() throws Exception {
        when(cartService.addItem(any(AddItemCommand.class)))
                .thenThrow(new OrderingException(OrderingErrorCode.CART_FORBIDDEN));

        mockMvc.perform(post("/api/v1/ordering/cart/items")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "skuId": "sku-1",
                          "qty": 1
                        }
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value("ORD_002"));
    }

    // ---------- update item ----------

    @Test
    void updateItemReturns200() throws Exception {
        when(cartService.updateItemQty(any(UpdateItemQtyCommand.class))).thenReturn(sampleCart(1));

        mockMvc.perform(put("/api/v1/ordering/cart/items/sku-1")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "newQty": 5
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void updateItemRejectsNegativeQty() throws Exception {
        mockMvc.perform(put("/api/v1/ordering/cart/items/sku-1")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "newQty": -1
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void updateItemWhenSkuNotInCartReturns404() throws Exception {
        when(cartService.updateItemQty(any(UpdateItemQtyCommand.class)))
                .thenThrow(new OrderingException(OrderingErrorCode.CART_ITEM_NOT_FOUND));

        mockMvc.perform(put("/api/v1/ordering/cart/items/missing")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "newQty": 1
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value("ORD_003"));
    }

    // ---------- remove item ----------

    @Test
    void removeItemReturns200() throws Exception {
        when(cartService.removeItem(any(RemoveItemCommand.class))).thenReturn(sampleCart(0));

        mockMvc.perform(delete("/api/v1/ordering/cart/items/sku-1"))
                .andExpect(status().isOk());

        verify(cartService).removeItem(any());
    }

    @Test
    void removeItemWhenNotFoundReturns404() throws Exception {
        when(cartService.removeItem(any(RemoveItemCommand.class)))
                .thenThrow(new OrderingException(OrderingErrorCode.CART_ITEM_NOT_FOUND));

        mockMvc.perform(delete("/api/v1/ordering/cart/items/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value("ORD_003"));
    }

    // ---------- clear cart ----------

    @Test
    void clearCartReturns200() throws Exception {
        when(cartService.clearCart(any(ClearCartCommand.class))).thenReturn(sampleCart(0));

        mockMvc.perform(delete("/api/v1/ordering/cart"))
                .andExpect(status().isOk());

        verify(cartService).clearCart(any());
    }

    // ---------- voucher ----------

    @Test
    void applyVoucherReturns200() throws Exception {
        when(cartService.applyVoucher(any(ApplyVoucherCommand.class))).thenReturn(sampleCart(1));

        mockMvc.perform(post("/api/v1/ordering/cart/voucher")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "voucherCode": "SAVE10"
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void applyVoucherRejectsBlankCode() throws Exception {
        mockMvc.perform(post("/api/v1/ordering/cart/voucher")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "voucherCode": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void removeVoucherReturns200() throws Exception {
        when(cartService.removeVoucher(any(RemoveVoucherCommand.class))).thenReturn(sampleCart(1));

        mockMvc.perform(delete("/api/v1/ordering/cart/voucher"))
                .andExpect(status().isOk());
    }
}
