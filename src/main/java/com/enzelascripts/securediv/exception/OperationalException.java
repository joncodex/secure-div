package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class OperationalException extends BaseException{

    public OperationalException(String message){
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
