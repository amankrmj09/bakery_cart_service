package com.blubugtech.bakery_cart_service.exception;

public class CartAlreadyMergedException extends RuntimeException {
    public CartAlreadyMergedException(String message) {
        super(message);
    }
}
