package ru.matveysuvorov.teamup.auth.dto;

import java.util.UUID;

public record AccountResponse(
        UUID uid,
        String email,
        boolean isVerified
) {
}
