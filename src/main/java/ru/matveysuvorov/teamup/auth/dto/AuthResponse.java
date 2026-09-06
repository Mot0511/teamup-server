package ru.matveysuvorov.teamup.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType
) {
}
