package com.aionn.ucp.infrastructure.persistence.adapter;

import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort;
import com.aionn.ucp.domain.model.CheckoutSessionStatus;
import com.aionn.ucp.infrastructure.persistence.entity.CheckoutSessionEntity;
import com.aionn.ucp.infrastructure.persistence.repository.CheckoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CheckoutSessionPersistenceAdapter implements CheckoutSessionPersistencePort {

    private final CheckoutSessionRepository jpa;

    @Override
    public Session save(Session session) {
        CheckoutSessionEntity entity = jpa.findById(session.sessionId()).orElseGet(CheckoutSessionEntity::new);
        entity.setSessionId(session.sessionId());
        entity.setUserId(session.userId());
        entity.setPlatformProfileUrl(session.platformProfileUrl());
        entity.setWebhookUrl(session.webhookUrl());
        entity.setStatus(session.status().name());
        entity.setCurrency(session.currency());
        entity.setLineItemsJson(session.lineItemsJson());
        entity.setTotalsJson(session.totalsJson());
        entity.setOrderId(session.orderId());
        entity.setCartId(session.cartId());
        entity.setContinueUrl(session.continueUrl());
        entity.setCreatedAt(session.createdAt());
        entity.setUpdatedAt(session.updatedAt());
        CheckoutSessionEntity saved = jpa.save(entity);
        return toSession(saved);
    }

    @Override
    public Optional<Session> findById(String sessionId) {
        return jpa.findById(sessionId).map(CheckoutSessionPersistenceAdapter::toSession);
    }

    @Override
    public Optional<Session> findByOrderId(String orderId) {
        return jpa.findFirstByOrderId(orderId).map(CheckoutSessionPersistenceAdapter::toSession);
    }

    @Override
    public Optional<Session> findByCartId(String cartId) {
        return jpa.findFirstByCartId(cartId).map(CheckoutSessionPersistenceAdapter::toSession);
    }

    private static Session toSession(CheckoutSessionEntity entity) {
        return new Session(
                entity.getSessionId(),
                entity.getUserId(),
                entity.getPlatformProfileUrl(),
                entity.getWebhookUrl(),
                CheckoutSessionStatus.valueOf(entity.getStatus()),
                entity.getCurrency(),
                entity.getLineItemsJson(),
                entity.getTotalsJson(),
                entity.getOrderId(),
                entity.getCartId(),
                entity.getContinueUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
