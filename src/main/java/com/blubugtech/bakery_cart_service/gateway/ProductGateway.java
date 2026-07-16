package com.blubugtech.bakery_cart_service.gateway;

import java.util.List;
import java.util.UUID;
import com.blubugtech.common.contract.feign.Product;
import com.blubugtech.common.contract.feign.ProductValidation;
import com.blubugtech.common.contract.feign.StockAvailability;

public interface ProductGateway {
    Product getProductById(UUID productId);
    boolean checkStock(UUID productId, int quantity);
    StockAvailability checkStockAvailability(UUID productId, int quantity);
    List<ProductValidation> validateProducts(List<UUID> productIds);
}
