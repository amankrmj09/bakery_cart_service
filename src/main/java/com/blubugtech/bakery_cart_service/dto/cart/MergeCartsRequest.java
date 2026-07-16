package com.blubugtech.bakery_cart_service.dto.cart;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MergeCartsRequest {

    // Getters and Setters
    @NotNull(message = "Source cart ID is required")
    private UUID sourceCartId;

    @NotNull(message = "Target cart ID is required")
    private UUID targetCartId;

    private Boolean deleteSourceCart = true; // Delete source cart after merge

    private Boolean handleDuplicates = true; // Merge quantities for duplicate products

    // Constructors
    public MergeCartsRequest() {}

    public MergeCartsRequest(UUID sourceCartId, UUID targetCartId) {
        this.sourceCartId = sourceCartId;
        this.targetCartId = targetCartId;
    }

}
