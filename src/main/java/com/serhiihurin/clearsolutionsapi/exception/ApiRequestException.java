package com.serhiihurin.clearsolutionsapi.exception;

import org.springframework.http.HttpStatus;

public class ApiRequestException extends ApiException{
    public final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    public ApiRequestException(String message) {
        super(message);
        super.httpStatus = httpStatus;
    }

    public ApiRequestException(String message, Throwable cause) {
        super(message, cause);
        super.httpStatus = httpStatus;
    }
}
