package ru.matveysuvorov.teamup.auth.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="reset_password_tokens")
public class ResetPasswordTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name="token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    @Column(name="expired_at", nullable = false)
    private Instant expiredAt;

    @Column(name="used_at")
    private Instant usedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }
}
