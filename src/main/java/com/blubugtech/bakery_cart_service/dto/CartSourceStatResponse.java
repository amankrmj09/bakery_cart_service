package com.blubugtech.bakery_cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSourceStatResponse {
    private String source;
    private Long cartCount;
    private Double averageValue;
    private Long convertedCount;
}
