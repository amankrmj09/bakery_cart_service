package com.blubugtech.bakery_cart_service.gateway.impl;

import com.blubugtech.bakery_cart_service.client.order.OrderServiceClient;
import com.blubugtech.bakery_cart_service.gateway.OrderGateway;
import com.blubugtech.bakery_cart_service.dto.order.OrderResponse;
import com.blubugtech.bakery_cart_service.dto.order.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FeignOrderGateway implements OrderGateway {

    private final OrderServiceClient orderServiceClient;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request, String userId, String role) {
        return orderServiceClient.createOrder(request, userId, role);
    }
}
