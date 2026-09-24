package ru.matveysuvorov.teamup.chats.entities;

import jakarta.persistence.*;
import org.springframework.data.annotation.TypeAlias;
import ru.matveysuvorov.teamup.games.GameEntity;
import ru.matveysuvorov.teamup.storage.FileEntity;
import ru.matveysuvorov.teamup.users.entities.UserEntity;

import javax.annotation.processing.Generated;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="chats")
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="is_team", nullable = false)
    private Boolean isTeam;

    @Column(name="is_public", nullable = false)
    private Boolean isPublic;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game")
    private GameEntity game;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="icon_id")
    private FileEntity icon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isTeam() {
        return this.isTeam;
    }

    public void setIsTeam(Boolean isTeam) {
        this.isTeam = isTeam;
    }

    public Boolean isPublic() {
        return this.isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public GameEntity getGame() {
        return game;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getTeam() {
        return isTeam;
    }

    public void setTeam(Boolean team) {
        isTeam = team;
    }

    public FileEntity getIcon() {
        return icon;
    }

    public void setIcon(FileEntity icon) {
        this.icon = icon;
    }
}
