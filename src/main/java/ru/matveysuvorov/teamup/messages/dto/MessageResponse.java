package ru.matveysuvorov.teamup.messages.dto;

import ru.matveysuvorov.teamup.messages.entities.MessageEntity;
import ru.matveysuvorov.teamup.messages.enums.MessageEvent;

public record MessageResponse(
    MessageEvent event,
    MessageEntity message
) {
}
