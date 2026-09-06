package ru.matveysuvorov.teamup.search.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SearchNotFoundException extends RuntimeException {
    public SearchNotFoundException(String message) {
        super(message);
    }
}
