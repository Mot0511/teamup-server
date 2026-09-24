package ru.matveysuvorov.teamup.messages.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="message_reads")
public class MessageReadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="user_id", nullable = false)
    private String userId;

    @Column(name="message_id", nullable = false)
    private Long messageId;

    @Column(name="chat_id", nullable = false)
    private Long chatId;

    @Column(name="readed_at", nullable = false)
    private Instant readedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Instant getReadedAt() {
        return readedAt;
    }

    public void setReadedAt(Instant readedAt) {
        this.readedAt = readedAt;
    }
}
