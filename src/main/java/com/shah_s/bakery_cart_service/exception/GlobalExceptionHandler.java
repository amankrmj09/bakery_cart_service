package com.shah_s.bakery_cart_service.exception;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.devofblue.common.exception.ErrorResponse;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CartServiceException.class)
    public ResponseEntity<ErrorResponse> handleCartServiceException(CartServiceException ex, WebRequest request) {
        logger.error("Cart service error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
            "CART_SERVICE_ERROR",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, WebRequest request) {
        logger.error("External service error: {}", ex.getMessage());

        String message = "External service unavailable";
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;

        if (ex.status() == 404) {
            message = "Requested resource not found";
            status = HttpStatus.NOT_FOUND;
        } else if (ex.status() == 400) {
            message = "Invalid request to external service";
            status = HttpStatus.BAD_REQUEST;
        }

        ErrorResponse error = new ErrorResponse(
            "EXTERNAL_SERVICE_ERROR",
            message,
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.status(status).body(error);
    }

}
