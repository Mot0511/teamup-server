package ru.matveysuvorov.teamup.users.entities;

import jakarta.persistence.*;

@Entity
@Table(name="friends")
public class FriendshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="from_user", nullable = false)
    private String fromUser;

    @Column(name="to_user", nullable = false)
    private String toUser;

    @Column(name="status", nullable = false)
    private Boolean status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
