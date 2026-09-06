package ru.matveysuvorov.teamup.search.dto;

public record SearchRequest(
        Integer game,
        String gender,
        Integer size,
        Integer age
) {
}
