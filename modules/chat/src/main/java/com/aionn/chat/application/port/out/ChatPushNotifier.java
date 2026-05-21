package com.aionn.chat.application.port.out;

/**
 * Bridge to the Notification module. When a message is sent and the
 * recipient is offline we ask Notification to render + dispatch a push
 * notification ({@code chat.message.received} event).
 */
public interface ChatPushNotifier {

    void notifyOffline(
            String recipientId,
            String conversationId,
            String senderDisplayName,
            String preview);
}

