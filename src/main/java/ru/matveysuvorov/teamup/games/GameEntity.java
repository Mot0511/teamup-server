package ru.matveysuvorov.teamup.games;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="games")
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="title", nullable = false)
    private String title;

    @Column(name="added_at", nullable = false)
    private Instant addedAt;

    @Column(name="game_cover_id", nullable = false)
    private UUID gameCoverId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public UUID getGameCoverId() {
        return gameCoverId;
    }

    public void setGameCoverId(UUID gameCoverId) {
        this.gameCoverId = gameCoverId;
    }
}
