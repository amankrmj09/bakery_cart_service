package com.blubugtech.bakery_cart_service.dto;

import com.blubugtech.bakery_cart_service.dto.order.OrderResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDto {
    private CartResponseDto cart;
    private OrderResponseDto order;
}
