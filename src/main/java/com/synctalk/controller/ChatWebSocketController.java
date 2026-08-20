package com.synctalk.controller;

import com.synctalk.config.DemoAccounts;
import com.synctalk.config.PresenceService;
import com.synctalk.model.ChatMessage;
import com.synctalk.model.User;
import com.synctalk.repository.ChatMessageRepository;
import com.synctalk.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
public class ChatWebSocketController {

    private static final int SNIPPET_LENGTH = 140;

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatMessageRepository chatMessageRepository,
                                    UserRepository userRepository,
                                    PresenceService presenceService,
                                    SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    public record OutgoingMessage(Long id, String senderUsername, String senderDisplayName, String content,
                                   String timestamp, Long replyToId, String replyToSenderDisplayName,
                                   String replyToContent) {
    }

    public record IncomingMessage(String content, Long replyToId) {
    }

    public record TypingSignal(boolean typing) {
    }

    @MessageMapping("/chat.send")
    public void send(IncomingMessage incoming, Principal principal) {
        if (principal == null || incoming.content() == null || incoming.content().isBlank()) {
            return;
        }
        User sender = userRepository.findByUsername(principal.getName()).orElse(null);
        if (sender == null) {
            return;
        }

        ChatMessage saved = new ChatMessage(sender.getUsername(), sender.getDisplayName(), incoming.content().trim());

        if (incoming.replyToId() != null) {
            chatMessageRepository.findById(incoming.replyToId()).ifPresent(original -> {
                saved.setReplyToId(original.getId());
                saved.setReplyToSenderDisplayName(original.getSenderDisplayName());
                saved.setReplyToContent(truncate(original.getContent()));
            });
        }

        chatMessageRepository.save(saved);

        messagingTemplate.convertAndSend("/topic/chat", toOutgoing(saved));
    }

    @MessageMapping("/chat.typing")
    public void typing(TypingSignal signal, Principal principal) {
        if (principal == null) {
            return;
        }
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/typing", Map.of(
                "username", user.getUsername(),
                "displayName", user.getDisplayName(),
                "typing", signal.typing()
        ));
    }

    @GetMapping("/api/messages")
    public List<OutgoingMessage> history() {
        return chatMessageRepository.findAllByOrderByTimestampAsc().stream()
                .map(this::toOutgoing)
                .toList();
    }

    @GetMapping("/api/presence")
    public List<Map<String, Object>> presence() {
        return DemoAccounts.ALL.stream()
                .map(account -> Map.<String, Object>of(
                        "username", account.username(),
                        "online", presenceService.isOnline(account.username())
                ))
                .toList();
    }

    private OutgoingMessage toOutgoing(ChatMessage m) {
        return new OutgoingMessage(
                m.getId(),
                m.getSenderUsername(),
                m.getSenderDisplayName(),
                m.getContent(),
                m.getTimestamp().toString(),
                m.getReplyToId(),
                m.getReplyToSenderDisplayName(),
                m.getReplyToContent()
        );
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= SNIPPET_LENGTH ? text : text.substring(0, SNIPPET_LENGTH) + "…";
    }
}
