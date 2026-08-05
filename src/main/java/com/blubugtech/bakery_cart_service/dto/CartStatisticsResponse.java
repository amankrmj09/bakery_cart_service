package com.blubugtech.bakery_cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartStatisticsResponse {
    private Long totalCarts;
    private Long activeCarts;
    private Long abandonedCarts;
    private Long convertedCarts;
    private BigDecimal averageCartValue;
    private Double averageItemCount;
    private Double conversionRate;
    private List<DailyCartStatResponse> dailyStats;
    private List<CartSourceStatResponse> sourceStats;
    private DateRangeResponse dateRange;
}
