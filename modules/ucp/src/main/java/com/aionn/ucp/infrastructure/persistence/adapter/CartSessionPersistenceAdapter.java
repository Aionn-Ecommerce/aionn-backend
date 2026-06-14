package com.aionn.ucp.infrastructure.persistence.adapter;

import com.aionn.ucp.application.port.out.CartSessionPersistencePort;
import com.aionn.ucp.infrastructure.persistence.entity.CartSessionEntity;
import com.aionn.ucp.infrastructure.persistence.repository.CartSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartSessionPersistenceAdapter implements CartSessionPersistencePort {

    private final CartSessionRepository jpa;

    @Override
    public CartSession save(CartSession session) {
        CartSessionEntity entity = CartSessionEntity.builder()
                .cartId(session.cartId())
                .userId(session.userId())
                .currency(session.currency())
                .lineItemsJson(session.lineItemsJson())
                .totalsJson(session.totalsJson())
                .continueUrl(session.continueUrl())
                .createdAt(session.createdAt())
                .updatedAt(session.updatedAt())
                .build();
        CartSessionEntity saved = jpa.save(entity);
        return toSession(saved);
    }

    @Override
    public Optional<CartSession> findById(String cartId) {
        return jpa.findById(cartId).map(this::toSession);
    }

    @Override
    public Optional<CartSession> findByUserId(String userId) {
        return jpa.findByUserId(userId).map(this::toSession);
    }

    @Override
    public void deleteById(String cartId) {
        jpa.deleteById(cartId);
    }

    private CartSession toSession(CartSessionEntity e) {
        return new CartSession(
                e.getCartId(),
                e.getUserId(),
                e.getCurrency(),
                e.getLineItemsJson(),
                e.getTotalsJson(),
                e.getContinueUrl(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }
}
