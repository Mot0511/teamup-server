package ru.matveysuvorov.teamup.users.dto;

public record CreateUserRequest(
        String username,
        String description,
        String gender,
        Integer age,
        Long game
) {
}
