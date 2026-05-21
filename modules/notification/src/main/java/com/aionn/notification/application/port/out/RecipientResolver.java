package com.aionn.notification.application.port.out;

import com.aionn.notification.domain.valueobject.NotificationChannel;

/**
 * Resolves the user's address for the requested channel (email, phone,
 * device token id). Implementations can hit identity service or query
 * a local read-model.
 */
public interface RecipientResolver {

    String resolve(String userId, NotificationChannel channel);
}

