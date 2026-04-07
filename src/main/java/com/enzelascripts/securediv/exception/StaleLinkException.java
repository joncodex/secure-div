package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class StaleLinkException extends BaseException{

    public StaleLinkException(String message){

        super(message, HttpStatus.GONE);
    }

    public StaleLinkException(){

        super("Link expired, request new document", HttpStatus.GONE);
    }
}
