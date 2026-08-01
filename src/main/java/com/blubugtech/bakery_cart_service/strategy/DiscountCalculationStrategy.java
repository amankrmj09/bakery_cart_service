package com.blubugtech.bakery_cart_service.strategy;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class DiscountCalculationStrategy {
    public BigDecimal calculateDiscount(String discountType, Double discountValue, BigDecimal subtotal) {
        if (discountType != null && discountValue != null) {
            BigDecimal discountAmt;
            if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
                discountAmt = subtotal.multiply(BigDecimal.valueOf(discountValue / 100.0));
            } else {
                discountAmt = BigDecimal.valueOf(discountValue);
            }
            if (discountAmt.compareTo(subtotal) > 0) {
                discountAmt = subtotal;
            }
            return discountAmt;
        }
        return BigDecimal.ZERO;
    }
}
