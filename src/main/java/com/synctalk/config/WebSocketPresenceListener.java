package com.synctalk.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;

/** Broadcasts a persona's online/offline status as WebSocket sessions open and close. */
@Component
public class WebSocketPresenceListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPresenceListener(PresenceService presenceService, SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        Principal user = principalOf(event);
        if (user == null) {
            return;
        }
        presenceService.markOnline(user.getName());
        broadcast(user.getName(), true);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user == null) {
            return;
        }
        presenceService.markOffline(user.getName());
        broadcast(user.getName(), false);
    }

    private Principal principalOf(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        return accessor.getUser();
    }

    private void broadcast(String username, boolean online) {
        messagingTemplate.convertAndSend("/topic/presence", Map.of("username", username, "online", online));
    }
}
