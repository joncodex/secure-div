package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class CreateResourceException extends BaseException{

    public CreateResourceException(String message){
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
