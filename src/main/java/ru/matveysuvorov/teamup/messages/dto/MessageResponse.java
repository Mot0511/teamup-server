package ru.matveysuvorov.teamup.messages.dto;

import ru.matveysuvorov.teamup.messages.entities.MessageEntity;
import ru.matveysuvorov.teamup.messages.enums.MessageEvent;

public class MessageResponse {

    public MessageEvent event;
    public Object payload;

    public MessageEvent getEvent() {
        return event;
    }

    public void setEvent(MessageEvent event) {
        this.event = event;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}