package com.blubugtech.bakery_cart_service.gateway;

import com.blubugtech.bakery_cart_service.dto.order.OrderResponse;
import com.blubugtech.bakery_cart_service.dto.order.CreateOrderRequest;

public interface OrderGateway {
    OrderResponse createOrder(CreateOrderRequest request, String userId, String role);
}
