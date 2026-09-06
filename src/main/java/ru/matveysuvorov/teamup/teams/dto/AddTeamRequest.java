package ru.matveysuvorov.teamup.teams.dto;

public record AddTeamRequest(
        String name,
        boolean isPublic
) {
}
