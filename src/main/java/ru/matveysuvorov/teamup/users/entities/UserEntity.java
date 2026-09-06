package ru.matveysuvorov.teamup.users.entities;

import jakarta.persistence.*;
import ru.matveysuvorov.teamup.storage.FileEntity;

import java.util.UUID;

@Entity
@Table(
        name="users",
        schema = "public"
)
public class UserEntity {

    @Id
    @Column(name="uid", nullable = false)
    private String uid;

    @Column(name="username", unique = true, nullable = false)
    private String username;

    @Column(name="email", unique = true, nullable = false)
    private String email;

    @Column(name="age", nullable = false)
    private int age;

    @Column(name="gender", nullable = false)
    private String gender;

    @Column(name="description")
    private String description;

    @Column(name="favourite_game")
    private Long favouriteGame;

    @Column(name="is_online")
    private Boolean online;

    @Column(name="avatar_id")
    private UUID avatar_id;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getFavouriteGame() {
        return favouriteGame;
    }

    public void setFavouriteGame(Long game) {
        this.favouriteGame = game;
    }

    public Boolean isOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Boolean getOnline() {
        return online;
    }

    public UUID getAvatar() {
        return avatar_id;
    }

    public void setAvatar(UUID avatar_id) {
        this.avatar_id = avatar_id;
    }
}
