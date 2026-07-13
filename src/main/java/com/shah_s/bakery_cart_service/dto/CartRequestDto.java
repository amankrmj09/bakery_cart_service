package com.shah_s.bakery_cart_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CartRequestDto {

    // Getters and Setters
    private UUID userId; // NULL for guest carts

    @Size(max = 255, message = "Session ID must not exceed 255 characters")
    private String sessionId;

    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String customerEmail;

    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode = "USD";

    @Size(max = 50, message = "Discount code must not exceed 50 characters")
    private String discountCode;

    private String specialInstructions;

    @Size(max = 20, message = "Delivery type must not exceed 20 characters")
    private String deliveryType; // PICKUP, DELIVERY

    private String deliveryAddress;

    @Size(max = 50, message = "Source must not exceed 50 characters")
    private String source; // WEB, MOBILE, API

    @Size(max = 20, message = "Device type must not exceed 20 characters")
    private String deviceType; // DESKTOP, MOBILE, TABLET

    private String userAgent;

    private Map<String, Object> metadata;

    // Constructors
    public CartRequestDto() {}

    public CartRequestDto(UUID userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public CartRequestDto(String sessionId) {
        this.sessionId = sessionId;
    }

}
