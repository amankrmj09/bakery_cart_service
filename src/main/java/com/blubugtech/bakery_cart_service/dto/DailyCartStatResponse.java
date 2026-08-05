package com.blubugtech.bakery_cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCartStatResponse {
    private String date;
    private Long convertedCount;
    private Long abandonedCount;
    private Double averageValue;
    private Double totalValue;
}
