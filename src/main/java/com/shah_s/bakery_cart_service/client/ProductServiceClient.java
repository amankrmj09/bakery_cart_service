package com.shah_s.bakery_cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "bakery-product-service", path = "/api")
public interface ProductServiceClient {

    @GetMapping("/products/{productId}")
    Map<String, Object> getProductById(@PathVariable UUID productId);

    @GetMapping("/products/batch")
    List<Map<String, Object>> getProductsByIds(@RequestParam("productIds") List<UUID> productIds);

    @GetMapping("/inventory/product/{productId}/availability")
    Map<String, Object> checkStockAvailability(@PathVariable("productId") UUID productId,
                                              @RequestParam("quantity") Integer quantity);

    @PostMapping("/inventory/product/{productId}/reserve")
    Map<String, Object> reserveStock(@PathVariable("productId") UUID productId,
                                   @RequestBody Map<String, Integer> request);

    @PostMapping("/inventory/product/{productId}/release-reserved")
    Map<String, Object> releaseStock(@PathVariable("productId") UUID productId,
                                   @RequestBody Map<String, Integer> request);

    @PostMapping("/products/batch/validate")
    List<Map<String, Object>> validateProducts(@RequestBody List<UUID> productIds);
}
