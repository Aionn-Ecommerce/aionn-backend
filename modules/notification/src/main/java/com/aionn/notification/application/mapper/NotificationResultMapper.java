package com.aionn.notification.application.mapper;

import com.aionn.notification.application.dto.notification.result.NotificationResult;
import com.aionn.notification.application.dto.provider.result.ProviderResult;
import com.aionn.notification.application.dto.subscription.result.DeviceTokenResult;
import com.aionn.notification.application.dto.subscription.result.SubscriptionResult;
import com.aionn.notification.application.dto.template.result.TemplateResult;
import com.aionn.notification.domain.model.DeviceToken;
import com.aionn.notification.domain.model.Notification;
import com.aionn.notification.domain.model.NotificationProvider;
import com.aionn.notification.domain.model.NotificationSubscription;
import com.aionn.notification.domain.model.NotificationTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NotificationResultMapper {

    public NotificationResult toResult(Notification n) {
        return new NotificationResult(
                n.getNotiId(), n.getUserId(), n.getTemplateId(),
                n.getChannel().name(), n.getCategory().name(), n.getPriority().name(),
                n.getSubject(), n.getContent(), n.getCampaignId(),
                n.getStatus().name(), n.getRetryCount(), n.getLastFailureReason(),
                n.getCreatedAt(), n.getUpdatedAt(),
                n.getSentAt(), n.getReadAt(), n.getDeletedAt());
    }

    public TemplateResult toResult(NotificationTemplate t) {
        return new TemplateResult(
                t.getTemplateId(), t.getEventType(),
                t.getChannel().name(), t.getCategory().name(), t.getLocale(),
                t.getSubject(), t.getContent(), t.getPlaceholders(), t.getVersion(),
                t.isActive(), t.getCreatedAt(), t.getUpdatedAt());
    }

    public SubscriptionResult toResult(NotificationSubscription s) {
        Map<String, Map<String, Boolean>> snapshot = new LinkedHashMap<>();
        s.snapshot().forEach((cat, map) -> {
            Map<String, Boolean> sub = new LinkedHashMap<>();
            map.forEach((ch, enabled) -> sub.put(ch.name(), enabled));
            snapshot.put(cat.name(), sub);
        });
        return new SubscriptionResult(s.getUserId(), snapshot, s.getCreatedAt(), s.getUpdatedAt());
    }

    public DeviceTokenResult toResult(DeviceToken t) {
        return new DeviceTokenResult(
                t.getTokenId(), t.getUserId(), t.getDeviceToken(), t.getOs(),
                t.isActive(), t.getRegisteredAt());
    }

    public ProviderResult toResult(NotificationProvider p) {
        return new ProviderResult(
                p.getProviderId(), p.getChannel().name(), p.getProviderType(), p.getConfig(),
                p.isActive(), p.getRateLimitPerMinute(), p.getConfiguredBy(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}

