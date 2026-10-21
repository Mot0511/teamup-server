package ru.matveysuvorov.teamup.auth.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="email_verification_codes")
public class EmailVerificationCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="account_id", nullable = false)
    private AccountEntity account;

    @Column(name="code", nullable = false)
    private Number code;

    @Column(name="expired_at", nullable = false)
    private Instant expiredAt;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

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

    public Number getCode() {
        return code;
    }

    public void setCode(Number code) {
        this.code = code;
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
}