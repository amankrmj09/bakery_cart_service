package com.blubugtech.bakery_cart_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class AddItemRequestDto {

    // Getters and Setters
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 50, message = "Quantity cannot exceed 50")
    private Integer quantity;

    @DecimalMin(value = "0.00", message = "Unit price override cannot be negative")
    private BigDecimal unitPriceOverride; // For custom pricing

    private String specialInstructions;

    @Size(max = 50, message = "Added from must not exceed 50 characters")
    private String addedFrom; // PRODUCT_PAGE, CATEGORY_PAGE, SEARCH, RECOMMENDATION

    private Map<String, Object> metadata;

    // Constructors
    public AddItemRequestDto() {}

    public AddItemRequestDto(UUID productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

}
