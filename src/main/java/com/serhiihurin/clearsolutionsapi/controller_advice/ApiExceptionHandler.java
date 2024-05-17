package com.serhiihurin.clearsolutionsapi.controller_advice;

import com.serhiihurin.clearsolutionsapi.dto.ApiExceptionDTO;
import com.serhiihurin.clearsolutionsapi.exception.ApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ApiExceptionHandler {
    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<ApiExceptionDTO> handleApiRequestException(ApiException exception) {
        ApiExceptionDTO apiExceptionDTO = new ApiExceptionDTO(
                exception.getMessage(),
                exception.getHttpStatus(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        log.warn("ApiException caught with HTTP status: {} and message: {}",
                exception.getHttpStatus(),
                exception.getMessage());
        return new ResponseEntity<>(apiExceptionDTO, exception.getHttpStatus());
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<ApiExceptionDTO> handleConstraintViolationException(ConstraintViolationException exception) {
        List<String> messages = exception.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessageTemplate)
                .toList();
        ApiExceptionDTO apiExceptionDTO = new ApiExceptionDTO(
                "Data validation failed: " + messages,
                HttpStatus.CONFLICT,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        log.warn("ConstraintViolationException caught with HTTP status: {} and message: {}",
                HttpStatus.CONFLICT,
                messages);
        return new ResponseEntity<>(apiExceptionDTO, HttpStatus.CONFLICT);
    }

//    @ExceptionHandler(value = Exception.class)
//    public ResponseEntity<ApiExceptionDTO> handleException(Exception exception) {
//        ApiExceptionDTO apiExceptionDTO = new ApiExceptionDTO(
//                exception.getMessage(),
//                HttpStatus.INTERNAL_SERVER_ERROR,
//                ZonedDateTime.now(ZoneId.of("Z"))
//        );
//        log.warn("Exception caught with HTTP status: {} and message: {}",
//                HttpStatus.INTERNAL_SERVER_ERROR,
//                exception.getMessage());
//        return new ResponseEntity<>(apiExceptionDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
