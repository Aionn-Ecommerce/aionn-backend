package com.aionn.ordering.infrastructure.integration;

import com.aionn.ordering.domain.valueobject.OrderStatus;
import com.aionn.ordering.infrastructure.persistence.repository.OrderJpaRepository;
import com.aionn.sharedkernel.integration.port.ordering.OrderQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * In-process adapter for the cross-context {@link OrderQueryPort}. Other
 * modules (e.g. catalog when closing a merchant) ask whether a merchant has
 * any non-terminal orders, and we answer by hitting the existing
 * {@code orders} table directly through Spring Data JPA.
 *
 * <p>
 * Lives in the ordering module so the JPA repository stays internal — the
 * caller only sees the shared-kernel port.
 */
@Component
@RequiredArgsConstructor
public class OrderingOrderQueryAdapter implements OrderQueryPort {

    private static final List<String> TERMINAL_STATUSES = Arrays.stream(OrderStatus.values())
            .filter(OrderStatus::isTerminal)
            .map(Enum::name)
            .toList();

    private final OrderJpaRepository orderJpaRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean hasOpenOrdersForMerchant(String merchantId) {
        if (merchantId == null || merchantId.isBlank()) {
            return false;
        }
        return orderJpaRepository.existsByMerchantIdAndStatusNotIn(merchantId, TERMINAL_STATUSES);
    }
}
