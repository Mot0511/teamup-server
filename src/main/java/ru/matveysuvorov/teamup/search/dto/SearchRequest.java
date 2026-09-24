package ru.matveysuvorov.teamup.search.dto;

public record SearchRequest(
        Long game,
        String gender,
        Integer size,
        Integer age
) {
}
