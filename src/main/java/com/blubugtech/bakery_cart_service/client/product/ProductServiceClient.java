package com.blubugtech.bakery_cart_service.client.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.blubugtech.common.contract.feign.Product;
import com.blubugtech.common.contract.feign.ProductValidation;
import com.blubugtech.common.contract.feign.StockAvailability;
import com.blubugtech.common.contract.messaging.StockOperationRequestPayload;
import com.blubugtech.common.contract.messaging.StockOperationResponsePayload;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "bakery-product-service", path = "/api", fallbackFactory = ProductServiceClientFallbackFactory.class)
public interface ProductServiceClient {

    @GetMapping("/products/{productId}")
    Product getProductById(@PathVariable UUID productId);

    @GetMapping("/products/batch")
    List<Product> getProductsByIds(@RequestParam("productIds") List<UUID> productIds);

    @GetMapping("/inventory/product/{productId}/availability")
    StockAvailability checkStockAvailability(@PathVariable("productId") UUID productId,
                                              @RequestParam("quantity") Integer quantity);

    @PostMapping("/inventory/product/{productId}/reserve")
    StockOperationResponsePayload reserveStock(@PathVariable("productId") UUID productId,
                                   @RequestBody StockOperationRequestPayload request);

    @PostMapping("/inventory/product/{productId}/release-reserved")
    StockOperationResponsePayload releaseStock(@PathVariable("productId") UUID productId,
                                   @RequestBody StockOperationRequestPayload request);

    @PostMapping("/products/batch/validate")
    List<ProductValidation> validateProducts(@RequestBody List<UUID> productIds);
}
