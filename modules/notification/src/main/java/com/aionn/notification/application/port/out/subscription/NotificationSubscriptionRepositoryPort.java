package com.aionn.notification.application.port.out.subscription;

import com.aionn.notification.domain.model.NotificationSubscription;

import java.util.Optional;

public interface NotificationSubscriptionRepositoryPort {

    NotificationSubscription save(NotificationSubscription subscription);

    Optional<NotificationSubscription> findByUserId(String userId);
}
