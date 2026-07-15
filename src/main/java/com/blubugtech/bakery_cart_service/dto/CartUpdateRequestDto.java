package com.blubugtech.bakery_cart_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CartUpdateRequestDto {

    // Getters and Setters
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String customerEmail;

    @Size(max = 50, message = "Discount code must not exceed 50 characters")
    private String discountCode;

    private String specialInstructions;

    @Size(max = 20, message = "Delivery type must not exceed 20 characters")
    private String deliveryType;

    private String deliveryAddress;

    private Map<String, Object> metadata;

    // Constructors
    public CartUpdateRequestDto() {}

}
