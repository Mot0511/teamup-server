package ru.matveysuvorov.teamup.messages.dto;

import java.util.Optional;

public record Message(
        Optional<Long> id,
        String text,
        String localDateTime,
        String zoneId,
        Optional<Integer> repliedMessage
) {
}
