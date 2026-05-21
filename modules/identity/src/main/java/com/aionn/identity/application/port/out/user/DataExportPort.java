package com.aionn.identity.application.port.out.user;

import com.aionn.identity.application.dto.user.view.DataExportRequestView;

/**
 * Port interface for data export operations.
 * Provides methods to manage user data export requests.
 */
public interface DataExportPort {

    /**
     * Save a data export request.
     *
     * @param userId the user ID
     * @return the created data export request view
     */
    DataExportRequestView save(String userId);

    /**
     * Check if a user has an active data export request.
     *
     * @param userId the user ID
     * @return true if an active request exists
     */
    boolean hasActiveRequest(String userId);
}

