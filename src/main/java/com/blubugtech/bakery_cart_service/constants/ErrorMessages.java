package com.blubugtech.bakery_cart_service.constants;

public final class ErrorMessages {
    private ErrorMessages() {}
    public static final String CART_NOT_FOUND = "Cart not found";
    public static final String ITEM_NOT_FOUND = "Item not found in cart";
    public static final String CART_EMPTY = "Cannot checkout an empty cart";
    public static final String CART_ALREADY_MERGED = "Cart has already been merged";
}
