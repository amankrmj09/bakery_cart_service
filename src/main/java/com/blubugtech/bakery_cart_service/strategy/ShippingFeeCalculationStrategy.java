package com.blubugtech.bakery_cart_service.strategy;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ShippingFeeCalculationStrategy {
    public BigDecimal calculateShippingFee(String deliveryType) {
        if ("DELIVERY".equals(deliveryType)) {
            return new BigDecimal("5.00");
        }
        return BigDecimal.ZERO;
    }
}
