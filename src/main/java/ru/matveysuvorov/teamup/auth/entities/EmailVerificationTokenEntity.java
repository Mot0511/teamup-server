package ru.matveysuvorov.teamup.auth.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="email_verification_tokens")
public class EmailVerificationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="account_id", nullable = false)
    private AccountEntity account;

    @Column(name="token_hash", unique = true, nullable = false)
    private String tokenHash;

    @Column(name="expired_at", nullable = false)
    private Instant expiredAt;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    @Column(name="used_at")
    private Instant usedAt;

    public Integer getId() {
        return id;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }
}