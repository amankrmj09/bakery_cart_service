package com.blubugtech.bakery_cart_service.gateway.impl;

import com.blubugtech.bakery_cart_service.client.product.ProductServiceClient;
import com.blubugtech.bakery_cart_service.gateway.ProductGateway;
import org.blubakery.common.feign.contract.feign.CouponValidationResponse;
import org.blubakery.common.feign.contract.feign.Product;
import org.blubakery.common.feign.contract.feign.ProductValidation;
import org.blubakery.common.feign.contract.feign.StockAvailability;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FeignProductGateway implements ProductGateway {

    private final ProductServiceClient productServiceClient;

    @Override
    public Product getProductById(UUID productId) {
        return productServiceClient.getProductById(productId);
    }

    @Override
    public boolean checkStock(UUID productId, int quantity) {
        StockAvailability stock = productServiceClient.checkStockAvailability(productId, quantity);
        return stock != null && Boolean.TRUE.equals(stock.getSufficient());
    }

    @Override
    public StockAvailability checkStockAvailability(UUID productId, int quantity) {
        return productServiceClient.checkStockAvailability(productId, quantity);
    }

    @Override
    public List<ProductValidation> validateProducts(List<UUID> productIds) {
        return productServiceClient.validateProducts(productIds);
    }

    @Override
    public CouponValidationResponse validateCoupon(String code, Double cartTotal) {
        return productServiceClient.validateCoupon(code, cartTotal);
    }
}
