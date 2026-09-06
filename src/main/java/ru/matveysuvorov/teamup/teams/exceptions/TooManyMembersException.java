package ru.matveysuvorov.teamup.teams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class TooManyMembersException extends RuntimeException {
    public TooManyMembersException(String message) {
        super(message);
    }
}
