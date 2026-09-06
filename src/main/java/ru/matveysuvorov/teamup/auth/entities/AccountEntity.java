package ru.matveysuvorov.teamup.auth.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "accounts",
    schema = "public"
)
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id", nullable = false)
    private UUID id;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name="password_hash")
    private String passwordHash;

    @Column(name="email_confirmed_at", nullable = false)
    private Boolean isEmailConfirmed;

    @Column(name="enabled", nullable = false)
    private Boolean enabled;

    @Column(name="craeted_at", nullable = false)
    private Instant craetedAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @Column(name="last_sign_in_at")
    private Instant lastSignInAt;

    public UUID getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Boolean isEmailConfirmed() {
        return isEmailConfirmed;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Instant getCraetedAt() {
        return craetedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastSignInAt() {
        return lastSignInAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setIsEmailConfirmed(Boolean isEmailConfirmed) {
        this.isEmailConfirmed = isEmailConfirmed;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setCraetedAt(Instant craetedAt) {
        this.craetedAt = craetedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setLastSignInAt(Instant lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
    }
}
