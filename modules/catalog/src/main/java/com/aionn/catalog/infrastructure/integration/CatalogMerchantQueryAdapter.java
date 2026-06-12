package com.aionn.catalog.infrastructure.integration;

import com.aionn.catalog.application.port.out.MerchantRepository;
import com.aionn.catalog.domain.model.Merchant;
import com.aionn.sharedkernel.integration.port.catalog.MerchantQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * In-process bridge so other modules can resolve a caller's merchantId via
 * the shared-kernel port instead of importing catalog's internal repository.
 * Mirrors the {@code OrderingOrderQueryAdapter} pattern.
 */
@Component
@RequiredArgsConstructor
public class CatalogMerchantQueryAdapter implements MerchantQueryPort {

    private final MerchantRepository merchantRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findMerchantIdByOwnerId(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            return Optional.empty();
        }
        return merchantRepository.findByOwnerId(ownerId).map(Merchant::getMerchantId);
    }
}
