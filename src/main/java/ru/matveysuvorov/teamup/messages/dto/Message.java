package ru.matveysuvorov.teamup.messages.dto;

import java.util.Optional;

public record Message(
        String text,
        String localDateTime,
        String zoneId,
        Optional<Integer> repliedMessage
) {
}
