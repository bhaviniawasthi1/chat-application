package com.synctalk.controller;

import com.synctalk.model.ChatMessage;
import com.synctalk.model.User;
import com.synctalk.repository.ChatMessageRepository;
import com.synctalk.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatMessageRepository chatMessageRepository,
                                    UserRepository userRepository,
                                    SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public record OutgoingMessage(String senderUsername, String senderDisplayName, String content, String timestamp) {
    }

    public record IncomingMessage(String content) {
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

        ChatMessage saved = chatMessageRepository.save(
                new ChatMessage(sender.getUsername(), sender.getDisplayName(), incoming.content().trim()));

        messagingTemplate.convertAndSend("/topic/chat", new OutgoingMessage(
                saved.getSenderUsername(),
                saved.getSenderDisplayName(),
                saved.getContent(),
                saved.getTimestamp().toString()
        ));
    }

    @ResponseBody
    @GetMapping("/api/messages")
    public List<OutgoingMessage> history() {
        return chatMessageRepository.findAllByOrderByTimestampAsc().stream()
                .map(m -> new OutgoingMessage(m.getSenderUsername(), m.getSenderDisplayName(), m.getContent(), m.getTimestamp().toString()))
                .toList();
    }
}
