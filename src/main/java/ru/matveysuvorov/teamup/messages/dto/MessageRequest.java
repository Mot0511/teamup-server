package ru.matveysuvorov.teamup.messages.dto;

import ru.matveysuvorov.teamup.messages.enums.MessageEvent;

import java.util.Optional;

public record MessageRequest(
        MessageEvent event,
        Message payload
) {
}