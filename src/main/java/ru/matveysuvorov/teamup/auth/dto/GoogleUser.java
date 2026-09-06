package ru.matveysuvorov.teamup.auth.dto;

public record GoogleUser(
        String user_id,
        String email
) {
}
