package ru.matveysuvorov.teamup.teams.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="invitations")
public class InviteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="number", nullable = false, unique = true)
    private Integer number;

    @Column(name="team_id", nullable = false, unique = true)
    private Integer teamId;

    @Column(name="expired_at", nullable = false)
    private Instant expiredAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }
}
