package com.serhiihurin.clearsolutionsapi.exception;

import org.springframework.http.HttpStatus;

public class UnsatisfiedAgeException extends ApiException{
    public final HttpStatus httpStatus = HttpStatus.FORBIDDEN;
    public UnsatisfiedAgeException(String message) {
        super(message);
        super.httpStatus = httpStatus;
    }

    public UnsatisfiedAgeException(String message, Throwable cause) {
        super(message, cause);
        super.httpStatus = httpStatus;
    }
}
