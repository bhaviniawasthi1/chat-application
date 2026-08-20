package com.synctalk.config;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks which demo personas currently have a live WebSocket connection open. */
@Component
public class PresenceService {

    private final Set<String> onlineUsernames = ConcurrentHashMap.newKeySet();

    public void markOnline(String username) {
        if (username != null) {
            onlineUsernames.add(username);
        }
    }

    public void markOffline(String username) {
        if (username != null) {
            onlineUsernames.remove(username);
        }
    }

    public boolean isOnline(String username) {
        return username != null && onlineUsernames.contains(username);
    }
}
