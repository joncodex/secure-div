package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class ImageConversionException extends BaseException{

    public ImageConversionException(String message){

        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
