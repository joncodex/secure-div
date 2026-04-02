package com.enzelascripts.securediv.exception;

import org.springframework.http.HttpStatus;

public class DownloadException extends BaseException {

    public DownloadException(String message) {

        super(message, HttpStatus.NOT_FOUND);
    }
}
