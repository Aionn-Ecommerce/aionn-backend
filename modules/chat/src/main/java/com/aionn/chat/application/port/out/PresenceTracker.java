package com.aionn.chat.application.port.out;

import java.util.Set;

/**
 * Tracks which users currently have an open WebSocket session. Used to
 * decide whether to deliver instantly or fall back to a push notification.
 */
public interface PresenceTracker {

    void markOnline(String userId, String sessionId);

    void markOffline(String userId, String sessionId);

    boolean isOnline(String userId);

    Set<String> filterOnline(Set<String> userIds);
}

