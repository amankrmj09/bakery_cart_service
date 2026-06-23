package com.shah_s.bakery_cart_service.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class FeignClientException extends RuntimeException {
    private final HttpStatus httpStatus;

    public FeignClientException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
