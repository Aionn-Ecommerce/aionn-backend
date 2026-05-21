package com.aionn.chat.infrastructure.notifier;

import com.aionn.chat.application.port.out.ChatPushNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default notifier impl: only logs the offline push. Used in dev/test where
 * the Notification module's full delivery pipeline is overkill. Activated by
 * {@code chat.push-notifier.provider=logging} (default when unset).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "chat.push-notifier", name = "provider", havingValue = "logging", matchIfMissing = true)
public class LoggingChatPushNotifier implements ChatPushNotifier {

    @Override
    public void notifyOffline(String recipientId, String conversationId,
            String senderDisplayName, String preview) {
        log.info("[chat-push] offline notify recipient={} conversation={} from={} preview={}",
                recipientId, conversationId, senderDisplayName, preview);
    }
}

