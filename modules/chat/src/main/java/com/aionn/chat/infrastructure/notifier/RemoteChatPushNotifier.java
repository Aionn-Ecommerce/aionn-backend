package com.aionn.chat.infrastructure.notifier;

import com.aionn.chat.application.port.out.ChatPushNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "chat.push-notifier", name = "provider", havingValue = "remote")
public class RemoteChatPushNotifier implements ChatPushNotifier {

    @Override
    public void notifyOffline(String recipientId, String conversationId,
            String senderDisplayName, String preview) {
        throw new UnsupportedOperationException(
                "RemoteChatPushNotifier not yet implemented. Wire NotificationDispatchService.sendByEvent here.");
    }
}

