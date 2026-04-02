package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class BadInputException extends BaseException {

    public BadInputException(String message) {

        super(message, HttpStatus.BAD_REQUEST);
    }
}
