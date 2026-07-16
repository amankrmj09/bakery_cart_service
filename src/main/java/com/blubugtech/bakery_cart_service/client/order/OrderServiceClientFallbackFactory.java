package com.blubugtech.bakery_cart_service.client.order;

import com.blubugtech.bakery_cart_service.dto.order.CreateOrderRequest;
import com.blubugtech.bakery_cart_service.dto.order.OrderResponse;
import com.blubugtech.common.exception.common.FeignClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderServiceClientFallbackFactory implements FallbackFactory<OrderServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceClientFallbackFactory.class);

    @Override
    public OrderServiceClient create(Throwable cause) {
        return new OrderServiceClient() {
            @Override
            public OrderResponse createOrder(CreateOrderRequest orderRequest, String userId, String userRole) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for createOrder: Order service unavailable", cause);
                OrderResponse response = new OrderResponse();
                response.setStatus("FAILED");
                return response;
            }
        };
    }
}
