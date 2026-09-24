package ru.matveysuvorov.teamup.chats.entities;

import jakarta.persistence.*;
import ru.matveysuvorov.teamup.users.entities.UserEntity;

import java.time.Instant;

@Entity
@Table(name="members")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    private UserEntity member;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="chat_id")
    private ChatEntity chat;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserEntity getMember() {
        return member;
    }

    public void setMember(UserEntity member) {
        this.member = member;
    }

    public ChatEntity getChat() {
        return chat;
    }

    public void setChat(ChatEntity chat) {
        this.chat = chat;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
