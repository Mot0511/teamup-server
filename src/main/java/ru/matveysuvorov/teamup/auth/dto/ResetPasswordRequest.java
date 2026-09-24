package ru.matveysuvorov.teamup.auth.dto;

public record ResetPasswordRequest(
        String token,
        String newPassword,
        String email
){
}