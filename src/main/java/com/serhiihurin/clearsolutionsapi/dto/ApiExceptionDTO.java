package com.serhiihurin.clearsolutionsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class ApiExceptionDTO {
    private String message;
    public HttpStatus httpStatus;
    private ZonedDateTime timestamp;
}
