package com.blubugtech.bakery_cart_service.exception;

import lombok.extern.slf4j.Slf4j;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import org.blubakery.common.core.exception.handler.BaseExceptionHandler;
import org.blubakery.common.core.exception.handler.ErrorResponse;

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
        return ResponseEntity.badRequest().body(ErrorResponse.builder().code("BAD_REQUEST").message(ex.getMessage()).timestamp(LocalDateTime.now()).path(request.getDescription(false)).build());
    }

    @ExceptionHandler({
        CartNotFoundException.class, 
        ItemNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(RuntimeException ex, WebRequest request) {
        log.error("Cart service not found error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder().code("NOT_FOUND").message(ex.getMessage()).timestamp(LocalDateTime.now()).path(request.getDescription(false)).build());
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
        return ResponseEntity.status(status).body(ErrorResponse.builder().code("EXTERNAL_SERVICE_ERROR").message(message).timestamp(LocalDateTime.now()).path(request.getDescription(false)).build());
    }
}
