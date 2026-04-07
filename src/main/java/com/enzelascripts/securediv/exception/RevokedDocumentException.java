package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class RevokedDocumentException extends BaseException{

    public RevokedDocumentException(String message){

        super(message, HttpStatus.FORBIDDEN);
    }

    public RevokedDocumentException(){

        super("Document revoked", HttpStatus.FORBIDDEN);
    }
}
