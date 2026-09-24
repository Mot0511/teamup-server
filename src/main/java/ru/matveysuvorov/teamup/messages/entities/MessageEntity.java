package ru.matveysuvorov.teamup.messages.entities;

import jakarta.persistence.*;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="user_id", nullable = false)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chat_id", nullable = false)
    private ChatEntity chat;

    @Column(name="text", nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="replied_message_id")
    private MessageEntity repliedMessage;

    @Column(name="attachment")
    private UUID attachment;

    @Column(name="sended_at", nullable = false)
    private Instant sendedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ChatEntity getChat() {
        return chat;
    }

    public void setChat(ChatEntity chat) {
        this.chat = chat;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MessageEntity getRepliedMessage() {
        return repliedMessage;
    }

    public void setRepliedMessage(MessageEntity repliedMessage) {
        this.repliedMessage = repliedMessage;
    }

    public UUID getAttachment() {
        return attachment;
    }

    public void setAttachment(UUID attachment) {
        this.attachment = attachment;
    }

    public Instant getSendedAt() {
        return sendedAt;
    }

    public void setSendedAt(Instant sendedAt) {
        this.sendedAt = sendedAt;
    }
}
