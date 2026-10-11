package ru.matveysuvorov.teamup.search.models;

import org.apache.catalina.User;
import ru.matveysuvorov.teamup.users.entities.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Search {

    private UUID id = UUID.randomUUID();
    private List<UserEntity> users = new ArrayList<UserEntity>();

    private Long game;
    private Integer age;
    private Integer size;
    private String gender;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<UserEntity> getUsers() {return users;}

    public void addUser(UserEntity user) {
        users.add(user);
    }

    public void removeUser(String uid) {
        users.removeIf(user -> user.getUid().equals(uid));
    }

    public void setUsers(List<UserEntity> users) {
        this.users = users;
    }

    public Long getGame() {
        return game;
    }

    public void setGame(Long game) {
        this.game = game;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
