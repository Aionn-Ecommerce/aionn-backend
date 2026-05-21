package com.aionn.identity.application.port.out.user;

import com.aionn.identity.application.dto.user.view.DeletionRequestView;

import java.util.Optional;

/**
 * Port interface for account deletion operations.
 * Provides methods to manage account deletion requests.
 */
public interface AccountDeletionPort {

    /**
     * Save an account deletion request.
     *
     * @param userId              the user ID
     * @param scheduledDeletionAt the scheduled deletion timestamp
     * @return the created deletion request view
     */
    DeletionRequestView save(String userId, java.time.LocalDateTime scheduledDeletionAt);

    /**
     * Find a pending deletion request for a user.
     *
     * @param userId the user ID
     * @return optional deletion request view
     */
    Optional<DeletionRequestView> findPendingByUserId(String userId);

    /**
     * Cancel a deletion request.
     *
     * @param userId the user ID
     */
    void cancel(String userId);
}

