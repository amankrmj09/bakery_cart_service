package com.shah_s.bakery_cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import org.devofblue.common.dto.ProductDto;
import org.devofblue.common.dto.ProductValidationDto;
import org.devofblue.common.dto.StockAvailabilityDto;
import org.devofblue.common.dto.StockOperationRequestDto;
import org.devofblue.common.dto.StockOperationResponseDto;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "bakery-product-service", path = "/api", fallbackFactory = ProductServiceClientFallbackFactory.class)
public interface ProductServiceClient {

    @GetMapping("/products/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);

    @GetMapping("/products/batch")
    List<ProductDto> getProductsByIds(@RequestParam("productIds") List<UUID> productIds);

    @GetMapping("/inventory/product/{productId}/availability")
    StockAvailabilityDto checkStockAvailability(@PathVariable("productId") UUID productId,
                                              @RequestParam("quantity") Integer quantity);

    @PostMapping("/inventory/product/{productId}/reserve")
    StockOperationResponseDto reserveStock(@PathVariable("productId") UUID productId,
                                   @RequestBody StockOperationRequestDto request);

    @PostMapping("/inventory/product/{productId}/release-reserved")
    StockOperationResponseDto releaseStock(@PathVariable("productId") UUID productId,
                                   @RequestBody StockOperationRequestDto request);

    @PostMapping("/products/batch/validate")
    List<ProductValidationDto> validateProducts(@RequestBody List<UUID> productIds);
}
