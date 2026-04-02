package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class CertificateExistsException extends BaseException{

    public CertificateExistsException(String message){
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
