package ru.matveysuvorov.teamup.messages.dto;

import ru.matveysuvorov.teamup.messages.enums.MessageEvent;

public record MessageResponseById(
        MessageEvent event,
        Integer messageId
) {
}
