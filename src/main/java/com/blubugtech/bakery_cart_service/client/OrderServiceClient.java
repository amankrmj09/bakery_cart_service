package com.blubugtech.bakery_cart_service.client;

import com.blubugtech.bakery_cart_service.dto.order.CreateOrderRequestDto;
import com.blubugtech.bakery_cart_service.dto.order.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "bakery-order-service", path = "/api/orders", fallbackFactory = OrderServiceClientFallbackFactory.class)
public interface OrderServiceClient {

    @PostMapping
    OrderResponseDto createOrder(@RequestBody CreateOrderRequestDto orderRequest,
                                 @RequestHeader(value = "X-User-Id", required = false) String userId,
                                 @RequestHeader(value = "X-User-Role", required = false) String userRole);
}
