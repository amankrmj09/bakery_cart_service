package com.blubugtech.bakery_cart_service.exception;

import lombok.extern.slf4j.Slf4j;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import org.blubakery.bakery_common_libs.exception.handler.BaseExceptionHandler;
import org.blubakery.bakery_common_libs.exception.handler.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler({
        CartServiceException.class, 
        CartAlreadyMergedException.class, 
        EmptyCartException.class, 
        CheckoutException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex, WebRequest request) {
        log.error("Cart service bad request error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", ex.getMessage(), LocalDateTime.now(), request.getDescription(false)));
    }

    @ExceptionHandler({
        CartNotFoundException.class, 
        ItemNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(RuntimeException ex, WebRequest request) {
        log.error("Cart service not found error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("NOT_FOUND", ex.getMessage(), LocalDateTime.now(), request.getDescription(false)));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, WebRequest request) {
        log.error("External service error: {}", ex.getMessage());
        String message = "External service unavailable";
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        if (ex.status() == 404) {
            message = "Requested resource not found";
            status = HttpStatus.NOT_FOUND;
        } else if (ex.status() == 400) {
            message = "Invalid request to external service";
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(new ErrorResponse("EXTERNAL_SERVICE_ERROR", message, LocalDateTime.now(), request.getDescription(false)));
    }
}
