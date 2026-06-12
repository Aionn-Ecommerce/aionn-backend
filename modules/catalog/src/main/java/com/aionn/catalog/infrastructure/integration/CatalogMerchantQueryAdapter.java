package com.aionn.catalog.infrastructure.integration;

import com.aionn.catalog.application.port.out.MerchantRepository;
import com.aionn.catalog.domain.model.Merchant;
import com.aionn.sharedkernel.integration.port.catalog.MerchantQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findOwnerIdByMerchantId(String merchantId) {
        if (merchantId == null || merchantId.isBlank()) {
            return Optional.empty();
        }
        return merchantRepository.findById(merchantId).map(Merchant::getOwnerId);
    }
}
