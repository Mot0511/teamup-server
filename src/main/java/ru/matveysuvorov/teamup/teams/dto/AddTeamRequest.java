package ru.matveysuvorov.teamup.teams.dto;

import java.util.List;

public record AddTeamRequest(
        String name,
        boolean isPublic,
        Long gameId,
        List<String> members
) {
}
