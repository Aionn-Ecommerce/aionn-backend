package com.aionn.identity.application.port.out.consent;

import com.aionn.identity.application.dto.consent.result.ConsentResult;
import com.aionn.identity.domain.model.UserConsent;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for user consent records. Operates exclusively on the
 * application/domain types so the application layer is free of infrastructure
 * coupling.
 */
public interface ConsentPersistencePort {

    /** Append a new consent decision. The previous record is left untouched. */
    ConsentResult append(UserConsent consent);

    /** Latest consent record (granted or revoked) for a user/type pair. */
    Optional<ConsentResult> findLatest(String userId, String consentType);

    /** Full history for a user, newest-first. */
    List<ConsentResult> findHistory(String userId);
}

