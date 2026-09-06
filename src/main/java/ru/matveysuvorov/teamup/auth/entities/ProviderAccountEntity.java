package ru.matveysuvorov.teamup.auth.entities;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="provider_accounts")
public class ProviderAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name="provider", nullable = false)
    private String provider;

    @Column(name="provider_user_id", nullable = false, unique = true)
    private String providerUserId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }
}
