package com.aionn.ordering.application.service;

import com.aionn.ordering.application.dto.cart.command.RemoveVoucherCommand;
import com.aionn.ordering.application.mapper.OrderingResultMapper;
import com.aionn.ordering.application.port.out.CartPersistencePort;
import com.aionn.ordering.domain.model.Cart;
import com.aionn.sharedkernel.application.port.EventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartPersistencePort cartRepository;

    @Mock
    private OrderingResultMapper mapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CartService cartService;

    @Test
    @DisplayName("removeVoucher() should clear voucher from cart and save changes")
    void removeVoucher_clearsVoucher_whenCartExists() {
        String userId = "user-123";
        Cart cart = new Cart("cart-1", userId, new HashMap<>(), "DISCOUNT50", null, null);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.removeVoucher(new RemoveVoucherCommand(userId));

        assertNull(cart.getVoucherCode());
        verify(cartRepository).save(cart);
        verify(eventPublisher).publish(anyCollection());
    }
}
