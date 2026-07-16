package com.blubugtech.bakery_cart_service.dto.checkout;
import com.blubugtech.bakery_cart_service.dto.cart.CartResponse;

import com.blubugtech.bakery_cart_service.dto.order.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private CartResponse cart;
    private OrderResponse order;
}
