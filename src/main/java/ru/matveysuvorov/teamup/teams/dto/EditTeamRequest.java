package ru.matveysuvorov.teamup.teams.dto;

import java.util.List;

public record EditTeamRequest(
        Integer team_id,
        String newName,
        List<String> addedMembers,
        List<String> removedMembers
) {
}
