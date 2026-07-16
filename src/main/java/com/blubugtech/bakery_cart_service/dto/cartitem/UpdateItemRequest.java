package com.blubugtech.bakery_cart_service.dto.cartitem;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UpdateItemRequest {

    // Getters and Setters
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 50, message = "Quantity cannot exceed 50")
    private Integer quantity;

    private String specialInstructions;

    private Map<String, Object> metadata;

    // Constructors
    public UpdateItemRequest() {}

    public UpdateItemRequest(Integer quantity) {
        this.quantity = quantity;
    }

}
