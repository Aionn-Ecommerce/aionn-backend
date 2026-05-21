package com.aionn.notification.infrastructure.persistence.repository;

import com.aionn.notification.infrastructure.persistence.entity.NotificationTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateJpaRepository extends JpaRepository<NotificationTemplateEntity, String> {
    Optional<NotificationTemplateEntity> findByEventTypeAndChannelAndLocale(
            String eventType, String channel, String locale);
}

