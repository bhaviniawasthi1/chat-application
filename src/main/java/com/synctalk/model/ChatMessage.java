package com.synctalk.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderUsername;

    @Column(nullable = false)
    private String senderDisplayName;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    /** Set when this message is a reply to an earlier one; the sender/snippet are
     *  snapshotted at send time so history rendering never needs a second lookup. */
    private Long replyToId;

    private String replyToSenderDisplayName;

    @Column(length = 200)
    private String replyToContent;

    public ChatMessage() {
    }

    public ChatMessage(String senderUsername, String senderDisplayName, String content) {
        this.senderUsername = senderUsername;
        this.senderDisplayName = senderDisplayName;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Long getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(Long replyToId) {
        this.replyToId = replyToId;
    }

    public String getReplyToSenderDisplayName() {
        return replyToSenderDisplayName;
    }

    public void setReplyToSenderDisplayName(String replyToSenderDisplayName) {
        this.replyToSenderDisplayName = replyToSenderDisplayName;
    }

    public String getReplyToContent() {
        return replyToContent;
    }

    public void setReplyToContent(String replyToContent) {
        this.replyToContent = replyToContent;
    }
}
