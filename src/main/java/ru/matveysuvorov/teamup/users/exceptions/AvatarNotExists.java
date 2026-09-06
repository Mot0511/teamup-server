package ru.matveysuvorov.teamup.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AvatarNotExists extends RuntimeException {
    public AvatarNotExists(String message) {
        super(message);
    }
}
