package com.enzelascripts.securediv.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException {
    private final HttpStatus statusCode;

    public BaseException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

}
