package ru.matveysuvorov.teamup.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class WrongVerificationCode extends RuntimeException {
    public WrongVerificationCode(String message) {
        super(message);
    }
}
