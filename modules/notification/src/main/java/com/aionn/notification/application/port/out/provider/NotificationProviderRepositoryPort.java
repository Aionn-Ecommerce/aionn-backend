package com.aionn.notification.application.port.out.provider;

import com.aionn.notification.domain.model.NotificationProvider;
import com.aionn.notification.domain.valueobject.NotificationChannel;

import java.util.List;
import java.util.Optional;

public interface NotificationProviderRepositoryPort {

    NotificationProvider save(NotificationProvider provider);

    Optional<NotificationProvider> findById(String providerId);

    Optional<NotificationProvider> findActiveByChannel(NotificationChannel channel);

    List<NotificationProvider> findAll();
}
