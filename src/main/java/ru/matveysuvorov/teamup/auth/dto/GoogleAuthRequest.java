package ru.matveysuvorov.teamup.auth.dto;

public record GoogleAuthRequest(
        String provider_id_token
) {
}
