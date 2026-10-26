package ru.matveysuvorov.teamup.stats.dto;

public record Stats(
        Long usersCount,
        Long teamsCount,
        Long publicTeamsCount
) {
}
