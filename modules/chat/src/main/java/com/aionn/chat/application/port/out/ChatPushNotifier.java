package com.aionn.chat.application.port.out;

public interface ChatPushNotifier {

    void notifyOffline(
            String recipientId,
            String conversationId,
            String senderDisplayName,
            String preview);
}

