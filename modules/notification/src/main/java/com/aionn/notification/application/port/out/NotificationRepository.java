package com.aionn.notification.application.port.out;

import com.aionn.notification.domain.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(String notiId);

    List<Notification> findByUser(String userId, int limit);

    /** UC8.3 retry sweep - PENDING with retryCount < max. */
    List<Notification> findRetryable(int limit);

    /** UC8.12 - aggregate by campaign and status. */
    long countByCampaignAndStatus(String campaignId, String status);
}

