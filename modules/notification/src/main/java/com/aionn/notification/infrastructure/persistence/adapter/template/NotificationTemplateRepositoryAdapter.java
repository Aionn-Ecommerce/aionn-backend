package com.aionn.notification.infrastructure.persistence.adapter.template;

import com.aionn.notification.application.port.out.NotificationTemplateRepository;
import com.aionn.notification.domain.model.NotificationTemplate;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import com.aionn.notification.infrastructure.persistence.entity.NotificationTemplateEntity;
import com.aionn.notification.infrastructure.persistence.mapper.NotificationTemplateDomainMapper;
import com.aionn.notification.infrastructure.persistence.repository.NotificationTemplateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationTemplateRepositoryAdapter implements NotificationTemplateRepository {

    private final NotificationTemplateJpaRepository jpa;
    private final NotificationTemplateDomainMapper mapper;

    @Override
    public NotificationTemplate save(NotificationTemplate template) {
        NotificationTemplateEntity existing = jpa.findById(template.getTemplateId()).orElse(null);
        return mapper.toDomain(jpa.save(mapper.toEntity(template, existing)));
    }

    @Override
    public Optional<NotificationTemplate> findById(String templateId) {
        return jpa.findById(templateId).map(mapper::toDomain);
    }

    @Override
    public Optional<NotificationTemplate> findByEventChannelLocale(
            String eventType, NotificationChannel channel, String locale) {
        return jpa.findByEventTypeAndChannelAndLocale(eventType, channel.name(), locale)
                .map(mapper::toDomain);
    }
}

