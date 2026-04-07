package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class StorageException extends BaseException{

    public StorageException(String message){

        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public StorageException(){

        super("Service Unavailable, retry later", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
