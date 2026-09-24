package ru.matveysuvorov.teamup.teams.dto;

import java.util.List;

public record EditTeamRequest(
        Long teamId,
        String newName,
        Long gameId,
        List<String> addedMembers,
        List<String> removedMembers
) {
}
