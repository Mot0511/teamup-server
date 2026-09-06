package ru.matveysuvorov.teamup.auth.dto;

public record GoogleAuthRequest(
        String user_id,
        String provider_id_token
) {
}
