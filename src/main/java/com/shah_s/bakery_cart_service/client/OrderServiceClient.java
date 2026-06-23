package com.shah_s.bakery_cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "bakery-order-service", path = "/api/orders")
public interface OrderServiceClient {

    @PostMapping
    Map<String, Object> createOrder(@RequestBody Map<String, Object> orderRequest,
                                   @RequestHeader(value = "X-User-Id", required = false) String userId,
                                   @RequestHeader(value = "X-User-Role", required = false) String userRole);
}
