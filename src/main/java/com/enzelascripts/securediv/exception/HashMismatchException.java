package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class HashMismatchException extends BaseException{

    public HashMismatchException(String message){

        super(message, HttpStatus.CONFLICT);
    }

    public HashMismatchException(){

        super("Document integrity check failed", HttpStatus.CONFLICT);
    }
}
