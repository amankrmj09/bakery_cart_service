package com.blubugtech.bakery_cart_service.gateway;

import java.util.List;
import java.util.UUID;

import org.blubakery.common.feign.contract.feign.CouponValidationResponse;
import org.blubakery.common.feign.contract.feign.Product;
import org.blubakery.common.feign.contract.feign.ProductValidation;
import org.blubakery.common.feign.contract.feign.StockAvailability;

public interface ProductGateway {
    Product getProductById(UUID productId);
    boolean checkStock(UUID productId, int quantity);
    StockAvailability checkStockAvailability(UUID productId, int quantity);
    List<ProductValidation> validateProducts(List<UUID> productIds);
    CouponValidationResponse validateCoupon(String code, Double cartTotal);
}
