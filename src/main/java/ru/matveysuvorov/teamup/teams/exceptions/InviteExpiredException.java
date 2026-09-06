package ru.matveysuvorov.teamup.teams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class InviteExpiredException extends RuntimeException {
    public InviteExpiredException(String message) {
        super(message);
    }
}
