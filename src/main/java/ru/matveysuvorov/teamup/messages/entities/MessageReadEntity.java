package ru.matveysuvorov.teamup.messages.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="message_reads")
public class MessageReadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="user_id", nullable = false)
    private String userId;

    @Column(name="message_id", nullable = false)
    private Integer messageId;

    @Column(name="chat_id", nullable = false)
    private Integer chatId;

    @Column(name="readed_at", nullable = false)
    private Instant readedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public Instant getReadedAt() {
        return readedAt;
    }

    public void setReadedAt(Instant readedAt) {
        this.readedAt = readedAt;
    }
}
