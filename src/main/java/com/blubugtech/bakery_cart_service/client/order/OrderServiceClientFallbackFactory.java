package com.blubugtech.bakery_cart_service.client.order;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_cart_service.dto.order.CreateOrderRequest;
import com.blubugtech.bakery_cart_service.dto.order.OrderResponse;
import org.blubakery.bakery_common_libs.exception.common.FeignClientException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderServiceClientFallbackFactory implements FallbackFactory<OrderServiceClient> {

    @Override
    public OrderServiceClient create(Throwable cause) {
        return (orderRequest, userId, userRole) -> {
            if (cause instanceof FeignClientException) throw (FeignClientException) cause;
            log.error("Fallback triggered for createOrder: Order service unavailable", cause);
            OrderResponse response = new OrderResponse();
            response.setStatus("FAILED");
            return response;
        };
    }
}
