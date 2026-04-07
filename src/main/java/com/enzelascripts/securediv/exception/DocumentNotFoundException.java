package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class DocumentNotFoundException extends BaseException{

    public DocumentNotFoundException(String message){

        super(message, HttpStatus.NOT_FOUND);
    }

    public DocumentNotFoundException(){

        super("Document Not Found", HttpStatus.NOT_FOUND);
    }
}
