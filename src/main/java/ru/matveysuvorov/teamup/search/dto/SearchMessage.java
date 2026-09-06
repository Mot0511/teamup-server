package ru.matveysuvorov.teamup.search.dto;

import ru.matveysuvorov.teamup.search.enums.SearchEvent;

public record SearchMessage(
    SearchEvent event,
    Object payload
) {
}
